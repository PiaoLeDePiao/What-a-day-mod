package dev.chasm.chasm.glue;

import dev.chasm.chasm.Chasm;
import dev.chasm.chasm.ChasmConfig;
import java.util.UUID;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.tick.TickEvent;

/**
 * FR-2：强力胶强化。
 * 1) 耐久：修改 create:super_glue 的默认 max_damage 组件（ModifyDefaultComponentsEvent）。
 * 2) 距离：Create 6.0.x 的主手/副手胶距离都读取玩家 BLOCK_INTERACTION_RANGE 属性，
 *    因此持有强力胶时给该属性加一个大修饰器即可实现"使用范围无上限"；放下即移除。
 */
@EventBusSubscriber(modid = Chasm.MODID)
public class GlueHandler {

    public static final ResourceLocation SUPER_GLUE_ID = ResourceLocation.fromNamespaceAndPath("create", "super_glue");
    private static final UUID REACH_UUID = UUID.fromString("0c0a5e0d-0000-4000-8000-000000000010");

    /** 在 mod 总线上调用（见 Chasm 主类）。 */
    public static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        if (!ChasmConfig.ENABLE_GLUE.get())
            return;
        Item glue = BuiltInRegistries.ITEM.get(SUPER_GLUE_ID);
        if (glue == null || glue == Items.AIR)
            return; // Create 未安装，静默跳过
        event.modify(glue, builder -> builder.set(DataComponents.MAX_DAMAGE, ChasmConfig.GLUE_MAX_DAMAGE.get()));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        if (!ChasmConfig.ENABLE_GLUE.get())
            return;
        Player player = event.player;
        if (player.level().isClientSide())
            return;

        boolean holdsGlue = isGlue(player.getMainHandItem()) || isGlue(player.getOffhandItem());
        AttributeInstance reach = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (reach == null)
            return;

        AttributeModifier existing = reach.getModifier(REACH_UUID);
        if (holdsGlue) {
            if (existing == null) {
                reach.addTransientModifier(new AttributeModifier(REACH_UUID, "chasm:glue_reach",
                    ChasmConfig.GLUE_REACH_BONUS.get(), AttributeModifier.Operation.ADD_VALUE));
            }
        } else if (existing != null) {
            reach.removeModifier(REACH_UUID);
        }
    }

    private static boolean isGlue(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(SUPER_GLUE_ID);
    }
}
