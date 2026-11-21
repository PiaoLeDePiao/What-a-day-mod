package dev.chasm.chasm.recipe;

import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * 1 paper + 1..8 gunpowder = firework rocket with flight duration = gunpowder count.
 * Vanilla's own special recipe still wins for 1..3 gunpowder, so this one only
 * ever handles 4..8 (up to flight duration 8).
 */
public class FireworkFlightRecipe extends CustomRecipe {

    public static final int MAX_POWDER = 8;
    public static final FireworkFlightRecipe INSTANCE = new FireworkFlightRecipe();

    public FireworkFlightRecipe() {
        super(CraftingBookCategory.MISC);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int paper = 0;
        int powder = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(Items.PAPER)) paper++;
            else if (stack.is(Items.GUNPOWDER)) powder++;
            else return false;
        }
        return paper == 1 && powder >= 1 && powder <= MAX_POWDER;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        int powder = 0;
        for (int i = 0; i < input.size(); i++) {
            if (input.getItem(i).is(Items.GUNPOWDER)) powder++;
        }
        ItemStack result = new ItemStack(Items.FIREWORK_ROCKET);
        result.set(DataComponents.FIREWORKS, new Fireworks(powder, List.of()));
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ChasmRecipes.FIREWORK_FLIGHT_SERIALIZER.get();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        ItemStack result = new ItemStack(Items.FIREWORK_ROCKET);
        result.set(DataComponents.FIREWORKS, new Fireworks(1, List.of()));
        return result;
    }

    public static class Serializer implements RecipeSerializer<FireworkFlightRecipe> {
        public static final MapCodec<FireworkFlightRecipe> CODEC = MapCodec.unit(INSTANCE);
        public static final StreamCodec<RegistryFriendlyByteBuf, FireworkFlightRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public MapCodec<FireworkFlightRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FireworkFlightRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
