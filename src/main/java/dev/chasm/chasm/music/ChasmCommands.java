package dev.chasm.chasm.music;

import dev.chasm.chasm.Chasm;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.DistExecutor;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** /chasm music play|stop|skip + /chasm adv（进度加载诊断）。 */
@EventBusSubscriber(modid = Chasm.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ChasmCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("chasm")
            .then(Commands.literal("music")
                .then(Commands.literal("play").executes(ctx -> {
                    runOnClient(() -> ChasmMusic.playNow(Minecraft.getInstance()));
                    ctx.getSource().sendSuccess(() -> Component.literal("已尝试播放背景音乐（仅单人模式生效）"), false);
                    return 1;
                }))
                .then(Commands.literal("stop").executes(ctx -> {
                    runOnClient(ChasmMusic::stopNow);
                    ctx.getSource().sendSuccess(() -> Component.literal("已停止背景音乐"), false);
                    return 1;
                }))
                .then(Commands.literal("skip").executes(ctx -> {
                    runOnClient(() -> {
                        ChasmMusic.stopNow();
                        ChasmMusic.playNow(Minecraft.getInstance());
                    });
                    ctx.getSource().sendSuccess(() -> Component.literal("已切换到下一首（仅单人模式生效）"), false);
                    return 1;
                })))
            .then(Commands.literal("adv").executes(ctx -> {
                int total = 0;
                Set<AdvancementType> tabs = new HashSet<>();
                for (AdvancementHolder holder : ctx.getSource().getServer().getAdvancements().getAllAdvancements()) {
                    total++;
                    holder.value().display().ifPresent(d -> tabs.add(d.type()));
                }
                Map<ResourceLocation, List<Resource>> files =
                        ctx.getSource().getServer().getResourceManager()
                                .listResourceStacks(ResourceLocation.fromNamespaceAndPath("", "advancements"), id -> true);
                String msg = "进度诊断：已加载进度 " + total + " 条、选项卡 " + tabs.size()
                        + " 个；数据包中共有 advancement 文件 " + files.size() + " 个。"
                        + "（文件数明显多于已加载数时，说明有进度文件加载失败，请把日志里以 "
                        + "'Couldn't load advancement' 开头的报错发回来）";
                ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
                return 1;
            })));
    }

    private static void runOnClient(Runnable runnable) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> runnable);
    }
}
