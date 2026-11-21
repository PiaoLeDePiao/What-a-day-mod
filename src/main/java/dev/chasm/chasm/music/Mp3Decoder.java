package dev.chasm.chasm.music;

import java.io.InputStream;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

/**
 * 基于 JLayer 的 mp3 帧解码器：逐帧解码为 16bit PCM。
 * 输出统一为双声道（单声道输入自动复制到两声道），简化 OpenAL 缓冲格式。
 */
public class Mp3Decoder implements AutoCloseable {
    private final Bitstream bitstream;
    private final Decoder decoder = new Decoder();
    private int frequency = 44100;
    private int channels = 2;

    public Mp3Decoder(InputStream in) {
        this.bitstream = new Bitstream(in);
    }

    /** 采样率（Hz），解码出第一帧后有效。 */
    public int getFrequency() {
        return frequency;
    }

    /** 声道数（恒为 2，输出已统一）。 */
    public int getChannels() {
        return channels;
    }

    /**
     * 解码下一帧。
     * @return 双声道交错的 PCM 数据；到达文件末尾返回 null。
     */
    public short[] decodeNextFrame() {
        try {
            Header header = bitstream.readFrame();
            if (header == null)
                return null;
            SampleBuffer buffer = (SampleBuffer) decoder.decodeFrame(header, bitstream);
            bitstream.closeFrame();
            if (buffer == null)
                return null;

            this.frequency = buffer.getSampleFrequency() > 0 ? buffer.getSampleFrequency() : 44100;
            short[] src = buffer.getBuffer();
            if (buffer.getChannelCount() >= 2) {
                // 已经是双声道，直接复制有效部分
                short[] out = new short[src.length];
                System.arraycopy(src, 0, out, 0, src.length);
                return out;
            }
            // 单声道 → 复制为双声道
            short[] out = new short[src.length * 2];
            for (int i = 0; i < src.length; i++) {
                out[i * 2] = src[i];
                out[i * 2 + 1] = src[i];
            }
            return out;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void close() {
        try {
            bitstream.close();
        } catch (Exception ignored) {
        }
    }
}
