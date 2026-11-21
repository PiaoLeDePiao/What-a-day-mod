package dev.chasm.chasm.music;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import org.lwjgl.openal.AL10;

/**
 * 用 LWJGL OpenAL 流式播放 mp3（3 个循环缓冲）。
 * 走游戏自身的音频设备，PC 与 FCL（Android）都可用；音量独立可调。
 */
public class OpenAlPlayer {
    private static final int BUFFER_COUNT = 3;
    private static final int SAMPLES_PER_BUFFER = 22050; // 约 0.5 秒 @44100

    private final int[] buffers = new int[BUFFER_COUNT];
    private int source = 0;
    private Mp3Decoder decoder;
    private boolean started;
    private boolean eof;
    private int queuedBuffers;
    private float volume;

    public OpenAlPlayer(Path mp3, float volume) {
        this.volume = volume;
        try {
            this.decoder = new Mp3Decoder(Files.newInputStream(mp3));
        } catch (Exception e) {
            this.decoder = null;
        }
    }

    /** 初始化 OpenAL 对象并预填缓冲。失败返回 false。 */
    public boolean start() {
        if (decoder == null)
            return false;
        try {
            AL10.alGenBuffers(buffers);
            source = AL10.alGenSources();
            AL10.alSourcei(source, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
            AL10.alSource3f(source, AL10.AL_POSITION, 0f, 0f, 0f);
            AL10.alSourcef(source, AL10.AL_GAIN, volume);
            for (int i = 0; i < BUFFER_COUNT; i++) {
                if (!fillBuffer(buffers[i]))
                    break;
            }
            if (queuedBuffers == 0) {
                close();
                return false;
            }
            AL10.alSourcePlay(source);
            started = true;
            return true;
        } catch (Exception e) {
            close();
            return false;
        }
    }

    /** 每 tick 调用：回收已播放缓冲并补充新数据。返回是否仍在播放。 */
    public boolean update() {
        if (!started)
            return false;
        int processed = AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED);
        while (processed-- > 0) {
            int buf = AL10.alSourceUnqueueBuffers(source);
            queuedBuffers--;
            if (!fillBuffer(buf)) {
                // EOF：不再补充
            }
        }
        if (queuedBuffers == 0 && eof) {
            close();
            return false;
        }
        return true;
    }

    private boolean fillBuffer(int buffer) {
        if (eof || decoder == null)
            return false;
        short[] pcm = new short[SAMPLES_PER_BUFFER * 2]; // 双声道交错
        int written = 0;
        while (written < pcm.length) {
            short[] frame = decoder.decodeNextFrame();
            if (frame == null) {
                eof = true;
                break;
            }
            int n = Math.min(frame.length, pcm.length - written);
            System.arraycopy(frame, 0, pcm, written, n);
            written += n;
        }
        if (written == 0)
            return false;
        ByteBuffer data = ByteBuffer.allocateDirect(written * 2).order(ByteOrder.nativeOrder());
        data.asShortBuffer().put(pcm, 0, written);
        AL10.alBufferData(buffer, AL10.AL_FORMAT_STEREO16, data, decoder.getFrequency());
        AL10.alSourceQueueBuffers(source, buffer);
        queuedBuffers++;
        return true;
    }

    public void setVolume(float volume) {
        this.volume = volume;
        if (started)
            AL10.alSourcef(source, AL10.AL_GAIN, volume);
    }

    public boolean isActive() {
        return started;
    }

    public void close() {
        if (started) {
            AL10.alSourceStop(source);
            started = false;
        }
        if (source != 0) {
            int processed = AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED);
            while (processed-- > 0)
                AL10.alSourceUnqueueBuffers(source);
            AL10.alDeleteSources(source);
            source = 0;
        }
        if (buffers[0] != 0) {
            AL10.alDeleteBuffers(buffers);
        }
        if (decoder != null) {
            decoder.close();
            decoder = null;
        }
    }
}
