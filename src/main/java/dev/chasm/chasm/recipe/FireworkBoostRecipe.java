package dev.chasm.chasm.recipe;

import com.mojang.serialization.MapCodec;
import dev.chasm.chasm.ChasmDataComponents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * 1 crafted firework rocket + 1..8 gunpowder = same rocket (keeps flight time,
 * colors and every other component) with chasm:boost_level = gunpowder count.
 * Higher boost level = stronger elytra speed kick when used while gliding.
 */
public class FireworkBoostRecipe extends CustomRecipe {

    public static final int MAX_POWDER = 8;
    public static final FireworkBoostRecipe INSTANCE = new FireworkBoostRecipe();

    public FireworkBoostRecipe() {
        super(CraftingBookCategory.MISC);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int rockets = 0;
        int powder = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(Items.FIREWORK_ROCKET)) rockets++;
            else if (stack.is(Items.GUNPOWDER)) powder++;
            else return false;
        }
        return rockets == 1 && powder >= 1 && powder <= MAX_POWDER;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack rocket = ItemStack.EMPTY;
        int powder = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(Items.FIREWORK_ROCKET)) rocket = stack.copy();
            else if (stack.is(Items.GUNPOWDER)) powder++;
        }
        rocket.set(ChasmDataComponents.BOOST_LEVEL, (byte) powder);
        return rocket;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ChasmRecipes.FIREWORK_BOOST_SERIALIZER.get();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        ItemStack result = new ItemStack(Items.FIREWORK_ROCKET);
        result.set(DataComponents.FIREWORKS, new net.minecraft.world.item.component.Fireworks(1, java.util.List.of()));
        result.set(ChasmDataComponents.BOOST_LEVEL, (byte) 1);
        return result;
    }

    public static class Serializer implements RecipeSerializer<FireworkBoostRecipe> {
        public static final MapCodec<FireworkBoostRecipe> CODEC = MapCodec.unit(INSTANCE);
        public static final StreamCodec<RegistryFriendlyByteBuf, FireworkBoostRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

        @Override
        public MapCodec<FireworkBoostRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FireworkBoostRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
