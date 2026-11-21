package dev.chasm.chasm.sinkhole;

import java.util.ArrayList;
import java.util.List;

import dev.chasm.chasm.ChasmConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlacementSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

/**
 * 天坑与原版结构的联动：大小、深度、内容物符合条件时，
 * 概率在坑底放置原版结构模板（村庄遗迹 / 远古城市中心 / 水下遗迹）。
 * 放置内容约束在天坑中心区块（32×32）内，因此不会被后续区块雕刻覆盖。
 * 海洋神殿等硬编码结构无法作为模板使用，故不支持（见 README）。
 */
public final class SinkholeStructures {

    private SinkholeStructures() {}

    /** 一次已规划的联动放置：模板路径（minecraft 命名空间）、相对坑心偏移、旋转、是否填水。 */
    public record Planned(String template, int dx, int dz, Rotation rotation, boolean flood) {}

    /** 确定性投骰子（由 params.seed() 派生，重载后结果一致）。 */
    public static List<Planned> plan(SinkholeParams params) {
        RandomSource r = RandomSource.create(params.seed() ^ 0x57A11C5L);
        List<Planned> out = new ArrayList<>();
        if (!ChasmConfig.SINKHOLE_STRUCTURES_ENABLED.get())
            return out;
        int max = ChasmConfig.SINKHOLE_STRUCTURES_MAX.get();
        if (max <= 0)
            return out;

        int radius = params.radiusX();
        int depth = params.depth();

        if (out.size() < max
            && radius >= ChasmConfig.SINKHOLE_STRUCTURES_VILLAGE_MIN_RADIUS.get()
            && r.nextDouble() < ChasmConfig.SINKHOLE_STRUCTURES_VILLAGE_CHANCE.get()) {
            planVillage(r, out);
        }
        if (out.size() < max
            && depth >= ChasmConfig.SINKHOLE_STRUCTURES_ANCIENT_CITY_MIN_DEPTH.get()
            && r.nextDouble() < ChasmConfig.SINKHOLE_STRUCTURES_ANCIENT_CITY_CHANCE.get()) {
            planAncientCity(r, out);
        }
        if (out.size() < max
            && r.nextDouble() < ChasmConfig.SINKHOLE_STRUCTURES_UNDERWATER_CHANCE.get()) {
            planUnderwaterRuins(r, out);
        }
        return out;
    }

    /** 村庄遗迹：钟楼/水井 + 3~5 栋小屋，约束在坑心半径 15 格内。 */
    private static void planVillage(RandomSource r, List<Planned> out) {
        out.add(new Planned("village/plains/town_centers/plains_meeting_point_1", 0, 0,
            Rotation.values()[r.nextInt(4)], false));
        String[] houses = {
            "village/plains/houses/plains_small_house_1",
            "village/plains/houses/plains_small_house_2",
            "village/plains/houses/plains_small_house_3",
            "village/plains/houses/plains_small_house_4",
            "village/plains/houses/plains_small_house_5"
        };
        int count = 3 + r.nextInt(3);
        int[][] slots = { {10, 0}, {-10, 0}, {0, 10}, {0, -10}, {7, 7}, {-7, -7}, {7, -7}, {-7, 7} };
        for (int i = slots.length - 1; i > 0; i--) {
            int j = r.nextInt(i + 1);
            int[] t = slots[i];
            slots[i] = slots[j];
            slots[j] = t;
        }
        for (int i = 0; i < count && i < slots.length; i++) {
            String h = houses[r.nextInt(houses.length)];
            out.add(new Planned(h, slots[i][0], slots[i][1], Rotation.values()[r.nextInt(4)], false));
        }
    }

    /** 远古城市：城市中心（不放监守者与尖啸体）。 */
    private static void planAncientCity(RandomSource r, List<Planned> out) {
        out.add(new Planned("ancient_city/city_center/city_center_" + (1 + r.nextInt(3)), 0, 0,
            Rotation.values()[r.nextInt(4)], false));
    }

    /** 水下遗迹：1~2 块大型遗迹，放置后填水。 */
    private static void planUnderwaterRuins(RandomSource r, List<Planned> out) {
        String[] ruins = {
            "underwater_ruin/big_brick_1",
            "underwater_ruin/big_cracked_1",
            "underwater_ruin/big_mossy_1",
            "underwater_ruin/big_warm_1"
        };
        out.add(new Planned(ruins[r.nextInt(ruins.length)], r.nextInt(9) - 4, r.nextInt(9) - 4,
            Rotation.values()[r.nextInt(4)], true));
        if (r.nextDouble() < 0.5) {
            out.add(new Planned(ruins[r.nextInt(ruins.length)], r.nextInt(15) - 7, r.nextInt(15) - 7,
                Rotation.values()[r.nextInt(4)], true));
        }
    }

    /** 在天坑中心区块雕刻完成后放置全部已规划模板。 */
    public static void place(WorldGenLevel level, List<Planned> plan, SinkholeParams params, int centerX, int centerZ) {
        if (plan.isEmpty())
            return;
        ServerLevel server = level.getLevel();
        boolean ocean = level.getBiome(new BlockPos(centerX, params.bottomY() + 1, centerZ))
            .is(BiomeTags.IS_OCEAN);

        for (Planned p : plan) {
            // 村庄只放陆地、水下遗迹只放海洋
            if (p.template().startsWith("village/") && ocean)
                continue;
            if (p.flood() && !ocean)
                continue;

            ResourceLocation id = ResourceLocation.parse("minecraft:" + p.template());
            StructureTemplate template = server.getStructureManager().getOrCreate(id);
            if (template.getSize().equals(Vec3i.ZERO))
                continue; // 模板缺失/版本变动：静默跳过

            BlockPos pos = new BlockPos(centerX + p.dx(), params.bottomY(), centerZ + p.dz());
            StructurePlacementSettings settings = new StructurePlacementSettings()
                .setRotation(p.rotation())
                .setMirror(Mirror.NONE);
            template.placeInWorld(level, pos, pos, settings, server.getRandom(), Block.UPDATE_CLIENTS);
            if (p.flood())
                flood(level, settings, template, pos, params);
        }

        boolean hasVillage = plan.stream().anyMatch(p -> p.template().startsWith("village/"));
        if (hasVillage && !ocean)
            scatterDeadBushes(level, params, centerX, centerZ);
    }

    /** 模板包围盒内把空气列填水（水下遗迹观感），水面留到距崖顶约 2 格。 */
    private static void flood(WorldGenLevel level, StructurePlacementSettings settings, StructureTemplate template,
                              BlockPos pos, SinkholeParams params) {
        BoundingBox bb = settings.getBoundingBox(pos, template.getSize());
        int surface = params.bottomY() + params.depth();
        int top = Math.min(bb.maxY() + 1, surface - 2);
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int x = bb.minX(); x <= bb.maxX(); x++) {
            for (int z = bb.minZ(); z <= bb.maxZ(); z++) {
                for (int y = params.bottomY() + 1; y <= top; y++) {
                    if (level.getBlockState(m.set(x, y, z)).isAir()) {
                        level.setBlock(m, Blocks.WATER.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
        }
    }

    /** 村庄遗迹周边的枯灌木点缀。 */
    private static void scatterDeadBushes(WorldGenLevel level, SinkholeParams params, int centerX, int centerZ) {
        RandomSource r = RandomSource.create(params.seed() ^ 0x57A11C5L ^ 0x1234L);
        int bottom = params.bottomY();
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int i = 0; i < 24; i++) {
            int x = centerX + r.nextInt(31) - 15;
            int z = centerZ + r.nextInt(31) - 15;
            if (level.getBlockState(m.set(x, bottom + 1, z)).isAir()
                && level.getBlockState(m.set(x, bottom, z)).canOcclude()) {
                level.setBlock(m.set(x, bottom + 1, z), Blocks.DEAD_BUSH.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }
}
