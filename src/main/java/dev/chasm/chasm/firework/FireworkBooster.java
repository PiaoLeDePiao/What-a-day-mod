package dev.chasm.chasm.firework;

import dev.chasm.chasm.Chasm;
import dev.chasm.chasm.ChasmConfig;
import dev.chasm.chasm.ChasmDataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.TickEvent;

/**
 * 烟花强化逻辑（仅服务端）：
 * - boost_level 烟花：使用瞬间给鞘翅滑翔更强的瞬时加速，等级越高越快；
 * - endless_boost 附魔烟花：使用后持续补充推进，直到玩家落地。
 */
public final class FireworkBooster {

    private static final String ENDLESS_KEY = "chasm:endless_boost";
    private static final ResourceKey<net.minecraft.world.item.enchantment.Enchantment> ENDLESS_ENCHANTMENT_KEY =
            ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(Chasm.MODID, "endless_boost"));

    private FireworkBooster() {
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!ChasmConfig.ENABLE_FIREWORKS.get()) return;
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty() || !stack.is(Items.FIREWORK_ROCKET)) return;
        if (!player.isFallFlying()) return;

        int boost = stack.getOrDefault(ChasmDataComponents.BOOST_LEVEL.get(), (byte) 0);
        boolean endless = ChasmConfig.FIREWORKS_ENDLESS_ENABLED.get()
                && BuiltInRegistries.ENCHANTMENT.getHolder(ENDLESS_ENCHANTMENT_KEY)
                        .map(holder -> EnchantmentHelper.getItemEnchantmentLevel(holder, stack) > 0)
                        .orElse(false);

        if (endless) {
            player.getPersistentData().putBoolean(ENDLESS_KEY, true);
        }
        if (boost > 0 && ChasmConfig.FIREWORKS_BOOST_ENABLED.get()) {
            double strength = ChasmConfig.FIREWORKS_BOOST_STRENGTH.get() * boost;
            Vec3 add = player.getLookAngle().scale(strength);
            Vec3 motion = clampHorizontal(player.getDeltaMovement().add(add), ChasmConfig.FIREWORKS_MAX_SPEED.get());
            player.setDeltaMovement(motion);
            player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), motion));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;
        if (!ChasmConfig.FIREWORKS_ENDLESS_ENABLED.get()) return;
        if (!player.getPersistentData().getBoolean(ENDLESS_KEY)) return;

        // 直到落地才解除无限推进
        if (player.onGround() || !player.isFallFlying()) {
            player.getPersistentData().remove(ENDLESS_KEY);
            return;
        }

        Vec3 motion = player.getDeltaMovement();
        double max = ChasmConfig.FIREWORKS_MAX_SPEED.get();
        double horizontal = Math.hypot(motion.x, motion.z);
        if (horizontal < max) {
            Vec3 thrust = player.getLookAngle().scale(ChasmConfig.FIREWORKS_ENDLESS_THRUST.get());
            motion = clampHorizontal(motion.add(thrust), max);
            player.setDeltaMovement(motion);
            if ((player.tickCount & 1) == 0) {
                player.connection.send(new ClientboundSetEntityMotionPacket(player.getId(), motion));
            }
        }
    }

    private static Vec3 clampHorizontal(Vec3 v, double max) {
        double h = Math.hypot(v.x, v.z);
        if (h > max) {
            double f = max / h;
            return new Vec3(v.x * f, v.y, v.z * f);
        }
        return v;
    }
}
