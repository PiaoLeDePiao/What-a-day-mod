package dev.chasm.chasm.potion;

import dev.chasm.chasm.Chasm;
import dev.chasm.chasm.ChasmConfig;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** FR-3：飞行药水注册与酿造配方。 */
public class ChasmPotions {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
        DeferredRegister.create(Registries.MOB_EFFECT, Chasm.MODID);
    public static final DeferredRegister<Potion> POTIONS =
        DeferredRegister.create(Registries.POTION, Chasm.MODID);

    public static final DeferredHolder<MobEffect, FlightEffect> FLIGHT_EFFECT =
        MOB_EFFECTS.register("flight", FlightEffect::new);

    public static final DeferredHolder<Potion, Potion> FLIGHT =
        POTIONS.register("chasm_flight", () -> new Potion(new MobEffectInstance(FLIGHT_EFFECT, ChasmConfig.FLIGHT_DURATION_TICKS.get())));
    public static final DeferredHolder<Potion, Potion> FLIGHT_LONG =
        POTIONS.register("chasm_flight_long", () -> new Potion(new MobEffectInstance(FLIGHT_EFFECT, ChasmConfig.FLIGHT_LONG_DURATION_TICKS.get())));

    /** 缓降药水 + 幻翼膜 → 飞行药水；飞行药水 + 红石 → 延长版。 */
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (!ChasmConfig.ENABLE_POTION.get())
                return;
            PotionBrewing.addMix(Potions.SLOW_FALLING, Items.PHANTOM_MEMBRANE, FLIGHT.get());
            PotionBrewing.addMix(FLIGHT.get(), Items.REDSTONE, FLIGHT_LONG.get());
        });
    }
}
