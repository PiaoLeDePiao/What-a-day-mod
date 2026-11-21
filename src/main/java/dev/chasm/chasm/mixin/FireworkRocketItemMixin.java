package dev.chasm.chasm.mixin;

import dev.chasm.chasm.ChasmConfig;
import net.minecraft.world.item.FireworkRocketItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 让烟花变成"可附魔"物品（附魔能力 1），
 * 配合数据驱动的 chasm:endless_boost 附魔（supported_items 为烟花）。
 */
@Mixin(FireworkRocketItem.class)
public class FireworkRocketItemMixin {

    @Inject(method = "getItemEnchantability", at = @At("HEAD"), cancellable = true)
    private void chasm$makeFireworksEnchantable(CallbackInfoReturnable<Integer> cir) {
        if (ChasmConfig.ENABLE_FIREWORKS.get()) {
            cir.setReturnValue(1);
        }
    }
}
