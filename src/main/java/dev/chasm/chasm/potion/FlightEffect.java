package dev.chasm.chasm.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 飞行状态效果。效果本身只作为标志与显示，真正的飞行能力由
 * FlightLogic（玩家 tick）维护，避免依赖效果生命周期钩子。
 */
public class FlightEffect extends MobEffect {
    public FlightEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x66CCFF);
    }
}
