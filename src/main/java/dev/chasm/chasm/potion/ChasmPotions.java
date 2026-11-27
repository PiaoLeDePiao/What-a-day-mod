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
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
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
    public static final int FLIGHT_TICKS = 36000;
    public static final int FLIGHT_LONG_TICKS = 144000;

    public static final DeferredHolder<Potion, Potion> FLIGHT =
        POTIONS.register("chasm_flight", () -> new Potion(new MobEffectInstance(FLIGHT_EFFECT, FLIGHT_TICKS)));
    public static final DeferredHolder<Potion, Potion> FLIGHT_LONG =
        POTIONS.register("chasm_flight_long", () -> new Potion(new MobEffectInstance(FLIGHT_EFFECT, FLIGHT_LONG_TICKS)));

    /** 缓降药水 + 幻翼膜 → 飞行药水；飞行药水 + 红石 → 延长版。 */
    public static void onRegisterBrewing(RegisterBrewingRecipesEvent event) {
        if (!ChasmConfig.ENABLE_POTION.get())
            return;
        PotionBrewing.Builder builder = event.getBuilder();
        builder.addMix(Potions.SLOW_FALLING, Items.PHANTOM_MEMBRANE, FLIGHT.getDelegate());
        builder.addMix(FLIGHT.getDelegate(), Items.REDSTONE, FLIGHT_LONG.getDelegate());
    }
}
