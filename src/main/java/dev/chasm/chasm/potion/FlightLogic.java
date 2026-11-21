package dev.chasm.chasm.potion;

import dev.chasm.chasm.Chasm;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.TickEvent;

/**
 * 飞行能力的实际维护：效果生效期间给玩家 mayfly（类似创造飞行），
 * 效果结束且确实由本模组授予时收回。只跑服务端，通过 abilities 同步给客户端。
 */
@EventBusSubscriber(modid = Chasm.MODID)
public class FlightLogic {

    private static final String GRANTED_KEY = "chasm:flight_granted";

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;
        Player player = event.player;
        if (player.level().isClientSide())
            return;

        boolean hasFlight = player.hasEffect(ChasmPotions.FLIGHT_EFFECT);
        boolean granted = player.getPersistentData().getBoolean(GRANTED_KEY);

        if (hasFlight) {
            if (!player.getAbilities().mayfly) {
                player.getAbilities().mayfly = true;
                player.getPersistentData().putBoolean(GRANTED_KEY, true);
                player.onUpdateAbilities();
            }
        } else if (granted) {
            // 效果结束：收回本模组授予的飞行（不影响创造/旁观与其他模组）
            if (!player.isCreative() && !player.isSpectator()) {
                player.getAbilities().mayfly = false;
                player.getAbilities().flying = false;
            }
            player.getPersistentData().putBoolean(GRANTED_KEY, false);
            player.onUpdateAbilities();
        }
    }
}
