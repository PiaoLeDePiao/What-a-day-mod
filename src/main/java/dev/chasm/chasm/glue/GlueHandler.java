package dev.chasm.chasm.glue;

import dev.chasm.chasm.Chasm;
import dev.chasm.chasm.ChasmConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * FR-2：强力胶强化（运行期，配置已加载后执行）。
 * 1) 耐久：每刻把背包内强力胶的 max_damage 同步为配置值（只写有变化的，避免每刻网络同步）。
 * 2) 距离：Create 6.0.x 的胶距离读取玩家 BLOCK_INTERACTION_RANGE 属性，
 *    持有强力胶时给该属性加大修饰器即"使用范围无上限"，放下即移除。
 */
@EventBusSubscriber(modid = Chasm.MODID)
public class GlueHandler {

    public static final ResourceLocation SUPER_GLUE_ID = ResourceLocation.fromNamespaceAndPath("create", "super_glue");
    private static final ResourceLocation REACH_ID = ResourceLocation.fromNamespaceAndPath("chasm", "glue_reach");

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!ChasmConfig.ENABLE_GLUE.get())
            return;
        Player player = event.getEntity();
        if (player.level().isClientSide())
            return;

        int maxDamage = ChasmConfig.GLUE_MAX_DAMAGE.get();
        for (ItemStack stack : player.getInventory().items) {
            if (isGlue(stack) && stack.getOrDefault(DataComponents.MAX_DAMAGE, -1) != maxDamage) {
                stack.set(DataComponents.MAX_DAMAGE, maxDamage);
            }
        }
        ItemStack offhand = player.getOffhandItem();
        if (isGlue(offhand) && offhand.getOrDefault(DataComponents.MAX_DAMAGE, -1) != maxDamage) {
            offhand.set(DataComponents.MAX_DAMAGE, maxDamage);
        }

        boolean holdsGlue = isGlue(player.getMainHandItem()) || isGlue(offhand);
        AttributeInstance reach = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (reach == null)
            return;

        AttributeModifier existing = reach.getModifier(REACH_ID);
        if (holdsGlue) {
            if (existing == null) {
                reach.addTransientModifier(new AttributeModifier(REACH_ID,
                    ChasmConfig.GLUE_REACH_BONUS.get(), AttributeModifier.Operation.ADD_VALUE));
            }
        } else if (existing != null) {
            reach.removeModifier(REACH_ID);
        }
    }

    private static boolean isGlue(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(SUPER_GLUE_ID);
    }
}
