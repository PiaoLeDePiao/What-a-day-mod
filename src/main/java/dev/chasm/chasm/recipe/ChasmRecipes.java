package dev.chasm.chasm.recipe;

import dev.chasm.chasm.Chasm;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ChasmRecipes {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Chasm.MODID);

    public static final Supplier<RecipeSerializer<FireworkFlightRecipe>> FIREWORK_FLIGHT_SERIALIZER =
            RECIPE_SERIALIZERS.register("firework_flight", FireworkFlightRecipe.Serializer::new);

    public static final Supplier<RecipeSerializer<FireworkBoostRecipe>> FIREWORK_BOOST_SERIALIZER =
            RECIPE_SERIALIZERS.register("firework_boost", FireworkBoostRecipe.Serializer::new);

    private ChasmRecipes() {
    }
}
