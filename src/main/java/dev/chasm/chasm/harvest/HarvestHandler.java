package dev.chasm.chasm.harvest;

import dev.chasm.chasm.ChasmConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.LootTableLoadEvent;

/**
 * 收割作物时极低概率额外掉落钻石 / 下界合金锭（默认 0.2% / 0.1%，配置可调）。
 * 作物 = #minecraft:crops 标签内方块 + 配置的额外方块列表。
 */
public final class HarvestHandler {

    private HarvestHandler() {
    }

    @SubscribeEvent
    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (!ChasmConfig.ENABLE_HARVEST.get()) return;
        ResourceLocation name = event.getName();
        if (!"minecraft".equals(name.getNamespace()) || !name.getPath().startsWith("blocks/")) return;
        Block block = BuiltInRegistries.BLOCK.get(
                ResourceLocation.fromNamespaceAndPath("minecraft", name.getPath().substring("blocks/".length())));
        if (block == null || !isCrop(block)) return;
        event.getTable().addPool(makePool());
    }

    private static boolean isCrop(Block block) {
        if (block.builtInRegistryHolder().is(BlockTags.CROPS)) return true;
        return ChasmConfig.HARVEST_EXTRA_CROPS.get().stream()
                .map(ResourceLocation::tryParse)
                .anyMatch(id -> id != null && block == BuiltInRegistries.BLOCK.get(id));
    }

    private static LootPool makePool() {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(LootItem.lootTableItem(Items.DIAMOND)
                        .when(LootItemRandomChanceCondition.randomChance(
                                ChasmConfig.HARVEST_DIAMOND_CHANCE.get().floatValue())))
                .add(LootItem.lootTableItem(Items.NETHERITE_INGOT)
                        .when(LootItemRandomChanceCondition.randomChance(
                                ChasmConfig.HARVEST_NETHERITE_CHANCE.get().floatValue())));
    }
}
