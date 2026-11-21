package dev.chasm.chasm.sinkhole;

import com.mojang.serialization.MapCodec;
import dev.chasm.chasm.ChasmConfig;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * FR-4：天坑结构。
 * 位置选择：在结构网格区块 ±3 内寻找"大片海洋/水域"（3×3 邻域海洋群系数最多的点）作为坑中心，
 * 找不到则用网格中心。出生点保护与大坑间距检查在 SinkholePiece 的 postProcess 中完成
 * （findGenerationPoint 阶段拿不到 StructureManager 与出生点）。
 */
public class SinkholeStructure extends Structure {

    public static final MapCodec<SinkholeStructure> CODEC = simpleCodec(SinkholeStructure::new);

    public SinkholeStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public StructureType<?> type() {
        return ChasmStructures.SINKHOLE_TYPE.get();
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        if (!ChasmConfig.ENABLE_SINKHOLE.get())
            return Optional.empty();

        ChunkPos chosen = pickChunk(ctx, ctx.chunkPos());
        int centerX = chosen.getMiddleBlockX();
        int centerZ = chosen.getMiddleBlockZ();

        int surfaceY = ctx.chunkGenerator().getFirstOccupiedHeight(
            centerX, centerZ, Heightmap.Types.OCEAN_FLOOR_WG, ctx.heightAccessor(), ctx.randomState());

        SinkholeParams params = SinkholeParams.sample(ctx.random(), surfaceY, ctx.heightAccessor().getMinBuildHeight());

        BlockPos pos = new BlockPos(centerX, surfaceY, centerZ);
        return Optional.of(new GenerationStub(pos, builder ->
            builder.addPiece(new SinkholePiece(ChasmStructures.SINKHOLE_PIECE.get(), centerX, centerZ, params))));
    }

    /** 在网格区块 ±3 内寻找 3×3 邻域海洋群系最多的区块作为坑中心。 */
    private ChunkPos pickChunk(GenerationContext ctx, ChunkPos gridCenter) {
        double oceanWeight = ChasmConfig.SINKHOLE_OCEAN_WEIGHT.get();
        if (oceanWeight <= 0.0)
            return gridCenter;

        ChunkPos best = gridCenter;
        double bestScore = -1;
        RandomSource random = ctx.random();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                ChunkPos candidate = new ChunkPos(gridCenter.x + dx, gridCenter.z + dz);
                int oceanCount = countOceanNeighbors(ctx, candidate);
                if (oceanCount < 5)
                    continue; // 要求 3×3 邻域内至少 5 个海洋区块（大片水域）
                double score = oceanCount * 1000.0 * oceanWeight + random.nextInt(1000);
                if (score > bestScore) {
                    bestScore = score;
                    best = candidate;
                }
            }
        }
        return best;
    }

    private int countOceanNeighbors(GenerationContext ctx, ChunkPos center) {
        int count = 0;
        for (int ox = -1; ox <= 1; ox++) {
            for (int oz = -1; oz <= 1; oz++) {
                int quartX = (center.x + ox) * 4;
                int quartZ = (center.z + oz) * 4;
                Holder<Biome> biome = ctx.chunkGenerator().getBiomeSource()
                    .getNoiseBiome(quartX, 16, quartZ, ctx.randomState().sampler());
                if (biome.is(BiomeTags.IS_OCEAN))
                    count++;
            }
        }
        return count;
    }
}
