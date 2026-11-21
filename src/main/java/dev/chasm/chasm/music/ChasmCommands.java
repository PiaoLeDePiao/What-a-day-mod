package dev.chasm.chasm.music;

import dev.chasm.chasm.Chasm;
import java.util.List;
import java.util.Map;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** 服务端命令：/chasm adv（进度加载诊断）。音乐控制是客户端命令，见 ChasmClientCommands。 */
@EventBusSubscriber(modid = Chasm.MODID, bus = EventBusSubscriber.Bus.GAME)
public class ChasmCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("chasm")
            .then(Commands.literal("adv").executes(ctx -> {
                int total = 0;
                int tabCount = 0;
                for (AdvancementHolder holder : ctx.getSource().getServer().getAdvancements().getAllAdvancements()) {
                    total++;
                    if (holder.value().display().isPresent())
                        tabCount++;
                }
                Map<ResourceLocation, List<Resource>> files =
                        ctx.getSource().getServer().getResourceManager()
                                .listResourceStacks("advancements", id -> true);
                String msg = "进度诊断：已加载进度 " + total + " 条、带显示选项卡 " + tabCount
                        + " 个；数据包中共有 advancement 文件 " + files.size() + " 个。"
                        + "（文件数明显多于已加载数时，说明有进度文件加载失败，请把日志里以 "
                        + "'Couldn't load advancement' 开头的报错发回来）";
                ctx.getSource().sendSuccess(() -> Component.literal(msg), false);
                return 1;
            })));
    }
}
