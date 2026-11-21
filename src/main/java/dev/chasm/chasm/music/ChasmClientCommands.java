package dev.chasm.chasm.music;

import dev.chasm.chasm.Chasm;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

/** 客户端命令：/chasm music play|stop|skip（直接操作本机音乐播放器，单人模式生效）。 */
@EventBusSubscriber(modid = Chasm.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class ChasmClientCommands {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("chasm")
            .then(Commands.literal("music")
                .then(Commands.literal("play").executes(ctx -> {
                    ChasmMusic.playNow(Minecraft.getInstance());
                    ctx.getSource().sendSuccess(() -> Component.literal("已尝试播放背景音乐（仅单人模式生效）"), false);
                    return 1;
                }))
                .then(Commands.literal("stop").executes(ctx -> {
                    ChasmMusic.stopNow();
                    ctx.getSource().sendSuccess(() -> Component.literal("已停止背景音乐"), false);
                    return 1;
                }))
                .then(Commands.literal("skip").executes(ctx -> {
                    ChasmMusic.stopNow();
                    ChasmMusic.playNow(Minecraft.getInstance());
                    ctx.getSource().sendSuccess(() -> Component.literal("已切换到下一首（仅单人模式生效）"), false);
                    return 1;
                }))));
    }
}
