package dev.chasm.chasm.music;

import dev.chasm.chasm.Chasm;
import dev.chasm.chasm.ChasmConfig;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.tick.TickEvent;

/**
 * FR-5：本地 mp3 背景音乐（客户端）。
 * 随机插入式：每 20 秒按配置概率尝试播放 config/chasm/music/ 下的一首 mp3；
 * 不重叠原版音乐时播放前停掉原版音乐，播完原版会自动续上。
 */
@EventBusSubscriber(modid = Chasm.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ChasmMusic {

    private static final int CHECK_INTERVAL_TICKS = 400; // 20 秒
    private static final Random RANDOM = new Random();

    private static OpenAlPlayer player;
    private static int tickCounter;
    private static long lastPlayEndedAt;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        Minecraft mc = Minecraft.getInstance();

        // 每 tick 维护当前播放器
        if (player != null) {
            player.setVolume(currentVolume(mc));
            if (!player.update()) {
                player = null;
                lastPlayEndedAt = System.currentTimeMillis();
            }
        }

        if (mc.level == null || mc.player == null) {
            stopNow();
            return;
        }
        if (!ChasmConfig.ENABLE_MUSIC.get()) {
            stopNow();
            return;
        }

        if (++tickCounter < CHECK_INTERVAL_TICKS)
            return;
        tickCounter = 0;

        if (player != null)
            return;
        tryPlay(mc);
    }

    /** 手动播放（/chasm music play 触发）。 */
    public static void playNow(Minecraft mc) {
        Path file = pickTrack();
        if (file == null)
            return;
        if (!ChasmConfig.MUSIC_OVERLAP_VANILLA.get())
            mc.getMusicManager().stopPlaying();
        start(file, mc);
    }

    public static void stopNow() {
        if (player != null) {
            player.close();
            player = null;
        }
        lastPlayEndedAt = System.currentTimeMillis();
    }

    private static void tryPlay(Minecraft mc) {
        long now = System.currentTimeMillis();
        long min = ChasmConfig.MUSIC_MIN_DELAY_SECONDS.get() * 1000L;
        long max = ChasmConfig.MUSIC_MAX_DELAY_SECONDS.get() * 1000L;
        if (now - lastPlayEndedAt < min)
            return;
        boolean overMax = now - lastPlayEndedAt >= max;
        if (!overMax && RANDOM.nextDouble() >= ChasmConfig.MUSIC_CHANCE.get())
            return;

        Path file = pickTrack();
        if (file == null)
            return;
        if (!ChasmConfig.MUSIC_OVERLAP_VANILLA.get())
            mc.getMusicManager().stopPlaying();
        start(file, mc);
    }

    private static void start(Path file, Minecraft mc) {
        OpenAlPlayer p = new OpenAlPlayer(file, currentVolume(mc));
        if (p.start()) {
            player = p;
        } else {
            lastPlayEndedAt = System.currentTimeMillis();
        }
    }

    private static Path pickTrack() {
        Path dir = FMLPaths.CONFIGDIR.get().resolve("chasm").resolve("music");
        List<Path> tracks = new ArrayList<>();
        try {
            if (!Files.isDirectory(dir))
                Files.createDirectories(dir);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path p : stream) {
                    String name = p.getFileName().toString().toLowerCase();
                    if (Files.isRegularFile(p) && name.endsWith(".mp3"))
                        tracks.add(p);
                }
            }
        } catch (Exception e) {
            return null;
        }
        if (tracks.isEmpty())
            return null;
        return tracks.get(RANDOM.nextInt(tracks.size()));
    }

    private static float currentVolume(Minecraft mc) {
        float music = mc.options.getSoundSourceVolume(SoundSource.MUSIC);
        return (float) ChasmConfig.MUSIC_VOLUME.get() * music;
    }
}
