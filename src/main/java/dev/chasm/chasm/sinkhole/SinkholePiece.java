package dev.chasm.chasm.sinkhole;

import java.util.List;

import dev.chasm.chasm.ChasmConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeCategory;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

/**
 * 天坑的雕刻与装饰。postProcess 按区块逐列执行：
 * 1) 坑内列挖空至碗底（边缘 0.80~阈值 为陡崖过渡带）；
 * 2) 坑缘紧邻列铺崖壁条带（深板岩/玄武岩/黑石/陶瓦等，按群系与噪声分层），并挖出侧壁凹洞；
 * 3) 底部按群系/概率铺生态（熔岩海、幽匿、珊瑚水底、沙漠残迹、繁茂、雪地地热）；
 * 4) 装饰：水瀑、发光藤蔓、沙柱、崖底碎石、残骸柱；
 * 5) 中心区块雕刻完成后，按计划放置联动结构（村庄/远古城市中心/水下遗迹）。
 * 生成前一次性检查出生点保护区与特大坑间距。
 * 形状由 ShapeField（种子驱动的涌现式半径场）计算。
 */
public class SinkholePiece extends StructurePiece {

    private int centerX;
    private int centerZ;
    private int startChunkX;
    private int startChunkZ;
    private SinkholeParams params;
    private ShapeField shapeField;

    private boolean decided;
    private boolean active;
    private boolean structuresDone;
    private List<SinkholeStructures.Planned> structurePlan;

    public SinkholePiece(StructurePieceType type, int centerX, int centerZ, SinkholeParams params) {
        super(type, 0, boundingBoxFor(centerX, centerZ, params));
        this.centerX = centerX;
        this.centerZ = centerZ;
        this.startChunkX = centerX >> 4;
        this.startChunkZ = centerZ >> 4;
        this.params = params;
        this.shapeField = new ShapeField(params);
    }

    public SinkholePiece(StructurePieceSerializationContext ctx, CompoundTag tag) {
        super(ChasmStructures.SINKHOLE_PIECE.get(), tag);
        // 字段由 readAdditionalSaveData 读取（在 super 构造中回调）
    }

    private static BoundingBox boundingBoxFor(int centerX, int centerZ, SinkholeParams p) {
        int r = Math.max(p.radiusX(), p.radiusZ()) + 24;
        return new BoundingBox(centerX - r, -64, centerZ - r, centerX + r, 320, centerZ + r);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putInt("CX", centerX);
        tag.putInt("CZ", centerZ);
        tag.putInt("Mode", params.mode());
        tag.putInt("Feature", params.featureType());
        tag.putDouble("FBlend", params.featureBlend());
        tag.putDouble("Spindle", params.spindle());
        tag.putDouble("Aspect", params.aspect());
        tag.putInt("RX", params.radiusX());
        tag.putInt("RZ", params.radiusZ());
        tag.putDouble("RCos", params.rotCos());
        tag.putDouble("RSin", params.rotSin());
        tag.putInt("Depth", params.depth());
        tag.putInt("BottomY", params.bottomY());
        tag.putLong("Seed", params.seed());
        tag.putBoolean("StructuresDone", structuresDone);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        centerX = tag.getInt("CX");
        centerZ = tag.getInt("CZ");
        startChunkX = centerX >> 4;
        startChunkZ = centerZ >> 4;
        params = new SinkholeParams(tag.getInt("Mode"), tag.getInt("Feature"),
            tag.getDouble("FBlend"), tag.getDouble("Spindle"), tag.getDouble("Aspect"),
            tag.getInt("RX"), tag.getInt("RZ"), tag.getDouble("RCos"), tag.getDouble("RSin"),
            tag.getInt("Depth"), tag.getInt("BottomY"), tag.getLong("Seed"));
        shapeField = new ShapeField(params);
        structuresDone = tag.getBoolean("StructuresDone");
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator,
                            RandomSource random, BoundingBox box, ChunkPos chunkPos, BlockPos piecePos) {
        if (!decided) {
            decided = true;
            active = canPlaceHere(level, structureManager);
            if (active && ChasmConfig.SINKHOLE_STRUCTURES_ENABLED.get())
                structurePlan = SinkholeStructures.plan(params);
        }
        if (!active)
            return;
        if (box == null || !box.intersects(chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(),
            chunkPos.getMaxBlockX(), chunkPos.getMaxBlockZ()))
            return;
        carve(level, chunkPos);
        // 中心区块雕刻完成后放置联动结构（模板均位于中心区块内，避免被后续雕刻挖掉）
        if (!structuresDone && chunkPos.x == startChunkX && chunkPos.z == startChunkZ) {
            structuresDone = true;
            if (structurePlan != null && !structurePlan.isEmpty())
                SinkholeStructures.place(level, structurePlan, params, centerX, centerZ);
        }
    }

    // ---------- 位置检查 ----------

    private boolean canPlaceHere(WorldGenLevel level, StructureManager structureManager) {
        ServerLevel server = level.getLevel();
        int protectChunks = ChasmConfig.SINKHOLE_SPAWN_PROTECTION_CHUNKS.get();
        if (protectChunks > 0) {
            BlockPos spawn = server.getSharedSpawnPos();
            int dx = Math.abs(startChunkX - (spawn.getX() >> 4));
            int dz = Math.abs(startChunkZ - (spawn.getZ() >> 4));
            if (Math.max(dx, dz) <= protectChunks)
                return false;
        }

        int lengthChunks = Math.max(params.radiusX(), params.radiusZ()) * 2 / 16;
        if (lengthChunks >= ChasmConfig.SINKHOLE_HUGE_THRESHOLD.get()) {
            int gap = ChasmConfig.SINKHOLE_HUGE_GAP_CHUNKS.get();
            for (int sx = -1; sx <= 1; sx++) {
                for (int sz = -1; sz <= 1; sz++) {
                    if (sx == 0 && sz == 0)
                        continue;
                    BlockPos p = new BlockPos(centerX + sx * gap * 16, 64, centerZ + sz * gap * 16);
                    StructureStart other = structureManager.getStructureWithPieceAt(p, ChasmStructures.SINKHOLE.get());
                    if (other != null && (other.getChunkPos().x != startChunkX || other.getChunkPos().z != startChunkZ))
                        return false;
                }
            }
        }
        return true;
    }

    // ---------- 主雕刻 ----------

    private void carve(WorldGenLevel level, ChunkPos chunkPos) {
        ChunkAccess chunk = level.getChunk(chunkPos.x, chunkPos.z);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        int minX = Math.max(chunkPos.getMinBlockX(), centerX - params.radiusX() - 8);
        int maxX = Math.min(chunkPos.getMaxBlockX(), centerX + params.radiusX() + 8);
        int minZ = Math.max(chunkPos.getMinBlockZ(), centerZ - params.radiusZ() - 8);
        int maxZ = Math.min(chunkPos.getMaxBlockZ(), centerZ + params.radiusZ() + 8);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int dx = x - centerX;
                int dz = z - centerZ;
                double rx = dx * params.rotCos() + dz * params.rotSin();
                double rz = -dx * params.rotSin() + dz * params.rotCos();
                double scaled = shapeScaled(rx, rz);
                if (scaled > 1.15)
                    continue;

                int surfaceY = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
                double threshold = edgeJitter(x, z);
                boolean inside = scaled < threshold;

                if (!inside) {
                    // 坑缘紧邻列：崖壁条带与凹洞
                    if (ChasmConfig.SINKHOLE_WALL_STRATA.get())
                        carveWallStrata(level, chunk, pos, x, z, surfaceY);
                    continue;
                }

                // 坑内列：挖空（0.80~阈值 为陡崖过渡带，底随 scaled 上升）
                int bottom = bottomYFor(scaled, surfaceY);
                for (int y = surfaceY; y > bottom; y--) {
                    chunk.setBlockState(pos.set(x, y, z), Blocks.AIR.defaultBlockState(), false);
                }
                decorate(level, chunk, pos, x, z, surfaceY, bottom, scaled);
            }
        }
    }

    /** 归一化形状距离：0 = 中心，1 ~ 边缘（边缘抖动在 carve 中另行叠加）。 */
    private double shapeScaled(double rx, double rz) {
        return shapeField.scaled(rx, rz);
    }

    /** 坑内列底部高度：中心（scaled < 0.80）全深，向边缘平滑抬升至地表。 */
    private int bottomYFor(double scaled, int surfaceY) {
        double t = Math.min(1.0, Math.max(0.0, scaled / 0.80));
        t = t * t;
        double full = surfaceY - params.bottomY();
        return (int) Math.round(surfaceY - full * (1.0 - t));
    }

    /** 边缘噪声扰动：坑缘阈值 0.92~1.06 起伏，形成参差崖线。 */
    private double edgeJitter(int x, int z) {
        double n = (hash(x, z) + hash(x + 1, z) + hash(x, z + 1) + hash(x + 1, z + 1)) / 4.0;
        return 0.92 + 0.14 * n;
    }

    private double hash(int x, int z) {
        long h = x * 374761393L + z * 668265263L;
        h = (h ^ (h >>> 13)) * 1274126177L;
        h ^= h >>> 16;
        return (h & 0xFFFF) / 65536.0;
    }

    private double hash3(int x, int y, int z) {
        return hash(x, z * 31 + y);
    }

    // ---------- 崖壁 ----------

    private void carveWallStrata(WorldGenLevel level, ChunkAccess chunk, BlockPos.MutableBlockPos pos,
                                 int x, int z, int surfaceY) {
        int wallBottom = params.bottomY() + (int) ((surfaceY - params.bottomY()) * 0.65);
        Holder<Biome> holder = level.getBiome(pos.set(x, surfaceY, z));
        Biome biome = holder.value();
        boolean badlands = holder.is(BiomeTags.IS_BADLANDS);
        boolean desert = biome.getBiomeCategory() == BiomeCategory.DESERT;
        boolean snowy = biome.coldEnoughToSnow(pos);

        for (int y = wallBottom; y <= surfaceY; y++) {
            int layer = (surfaceY - y) / 4;
            double h = hash3(x, layer, z);
            BlockState state = strataBlock(h, desert, badlands, snowy, surfaceY - y);
            chunk.setBlockState(pos.set(x, y, z), state, false);

            // 崖壁凹洞（千沟万壑）：小概率挖出侧壁空洞
            if (y > wallBottom + 5 && y < surfaceY - 3 && hash3(x, y / 2, z) < 0.02) {
                chunk.setBlockState(pos.set(x, y, z), Blocks.AIR.defaultBlockState(), false);
            }
        }
    }

    private BlockState strataBlock(double h, boolean desert, boolean badlands, boolean snowy, int belowSurface) {
        if (snowy && belowSurface <= 3)
            return Blocks.SNOW_BLOCK.defaultBlockState();
        if (desert) {
            if (h < 0.30) return Blocks.SANDSTONE.defaultBlockState();
            if (h < 0.40) return Blocks.SAND.defaultBlockState();
        }
        if (badlands) {
            if (h < 0.30) return Blocks.RED_TERRACOTTA.defaultBlockState();
            if (h < 0.45) return Blocks.ORANGE_TERRACOTTA.defaultBlockState();
            if (h < 0.55) return Blocks.BROWN_TERRACOTTA.defaultBlockState();
        }
        if (h < 0.25) return Blocks.DEEPSLATE.defaultBlockState();
        if (h < 0.45) return Blocks.STONE.defaultBlockState();
        if (h < 0.60) return Blocks.BASALT.defaultBlockState();
        if (h < 0.70) return Blocks.SMOOTH_BASALT.defaultBlockState();
        if (h < 0.80) return Blocks.BLACKSTONE.defaultBlockState();
        if (h < 0.90) return Blocks.COBBLED_DEEPSLATE.defaultBlockState();
        return Blocks.TUFF.defaultBlockState();
    }

    // ---------- 装饰 ----------

    private void decorate(WorldGenLevel level, ChunkAccess chunk, BlockPos.MutableBlockPos pos,
                          int x, int z, int surfaceY, int bottom, double scaled) {
        boolean ocean = level.getBiome(pos.set(x, surfaceY, z)).is(BiomeTags.IS_OCEAN);
        boolean desert = level.getBiome(pos).value().getBiomeCategory() == BiomeCategory.DESERT;
        boolean jungle = level.getBiome(pos).is(BiomeTags.IS_JUNGLE);

        // 水瀑：崖带列概率变成水柱倾泻而下
        if (scaled > 0.80 && ChasmConfig.SINKHOLE_WATERFALL_CHANCE.get() > 0) {
            double p = ChasmConfig.SINKHOLE_WATERFALL_CHANCE.get() * (ocean ? 2.0 : 1.0) * 0.25;
            if (hash(x, z) < p) {
                for (int y = surfaceY; y > bottom + 2; y--) {
                    chunk.setBlockState(pos.set(x, y, z), Blocks.WATER.defaultBlockState(), false);
                }
            }
        }

        // 发光藤蔓：陡崖带挂壁
        if (scaled > 0.82 && ChasmConfig.SINKHOLE_VINE_CHANCE.get() > 0) {
            double p = ChasmConfig.SINKHOLE_VINE_CHANCE.get() * (jungle ? 2.0 : 1.0);
            for (int y = surfaceY - 1; y > bottom + 6; y -= 3) {
                if (hash3(x, y, z) < p * 0.6) {
                    chunk.setBlockState(pos.set(x, y, z), Blocks.GLOW_LICHEN.defaultBlockState(), false);
                }
            }
        }

        // 沙漠：倒挂沙柱（放置后自然坍塌堆积，形成沙漏效果）
        if (desert && scaled > 0.90 && ChasmConfig.SINKHOLE_SAND_PILLAR_CHANCE.get() > 0) {
            if (hash(x, z) < ChasmConfig.SINKHOLE_SAND_PILLAR_CHANCE.get() * 0.35) {
                int len = 3 + (int) (hash3(x, 7, z) * 9);
                for (int y = surfaceY; y > surfaceY - len && y > bottom; y--) {
                    chunk.setBlockState(pos.set(x, y, z), Blocks.SAND.defaultBlockState(), false);
                }
            }
        }

        // 崖底碎石堆
        if (scaled > 0.80 && ChasmConfig.SINKHOLE_DEBRIS_EDGE_CHANCE.get() > 0) {
            if (hash3(x, 13, z) < ChasmConfig.SINKHOLE_DEBRIS_EDGE_CHANCE.get()) {
                int pile = 1 + (int) (hash3(x, 17, z) * 4);
                BlockState debris = hash3(x, 19, z) < 0.4 ? Blocks.GRAVEL.defaultBlockState()
                    : Blocks.COBBLESTONE.defaultBlockState();
                for (int y = bottom + 1; y <= bottom + pile; y++) {
                    chunk.setBlockState(pos.set(x, y, z), debris, false);
                }
            }
        }

        // 悬空残骸柱（简化：崖壁平台石柱 + 顶部木板）
        if (scaled > 0.60 && scaled < 0.92 && ChasmConfig.SINKHOLE_RUINS_CHANCE.get() > 0) {
            if (hash(x >> 3, z >> 3) < ChasmConfig.SINKHOLE_RUINS_CHANCE.get() * 0.25 && hash(x, z) < 0.04) {
                int top = bottom + 12 + (int) (hash3(x, 23, z) * 10);
                for (int y = bottom; y <= top; y++) {
                    chunk.setBlockState(pos.set(x, y, z),
                        hash3(x, y, z) < 0.5 ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState(), false);
                }
                chunk.setBlockState(pos.set(x, top + 1, z), Blocks.OAK_PLANKS.defaultBlockState(), false);
                chunk.setBlockState(pos.set(x, top + 2, z), Blocks.OAK_FENCE.defaultBlockState(), false);
            }
        }

        // 底部生态（坑底核心区）
        if (ChasmConfig.SINKHOLE_BOTTOM_ECOLOGY.get() && bottom - params.bottomY() <= 1) {
            applyBottomEcology(level, chunk, pos, x, z, bottom);
        }
    }

    private void applyBottomEcology(WorldGenLevel level, ChunkAccess chunk, BlockPos.MutableBlockPos pos,
                                    int x, int z, int bottom) {
        Holder<Biome> holder = level.getBiome(pos.set(x, bottom + 1, z));
        Biome biome = holder.value();
        double h = hash(x, z);
        double lavaChance = ChasmConfig.SINKHOLE_LAVA_SEA_CHANCE.get();
        double sculkChance = ChasmConfig.SINKHOLE_SCULK_CHANCE.get();

        if (lavaChance > 0 && h < lavaChance) {
            // 熔岩海：坑底岩浆 + 岩浆块环 + 少量下界菌类
            boolean magma = hash3(x, 3, z) > 0.88;
            chunk.setBlockState(pos.set(x, bottom, z), magma ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.LAVA.defaultBlockState(), false);
            if (magma && hash3(x, 5, z) < 0.12) {
                chunk.setBlockState(pos.set(x, bottom + 1, z),
                    hash3(x, 6, z) < 0.5 ? Blocks.CRIMSON_FUNGUS.defaultBlockState() : Blocks.WARPED_FUNGUS.defaultBlockState(), false);
            }
            return;
        }
        if (sculkChance > 0 && h < lavaChance + sculkChance) {
            // 深暗风：幽匿块 + 脉络 + 少量传感器
            chunk.setBlockState(pos.set(x, bottom, z), Blocks.SCULK.defaultBlockState(), false);
            if (hash3(x, 9, z) < 0.20) {
                chunk.setBlockState(pos.set(x, bottom + 1, z), Blocks.SCULK_VEIN.defaultBlockState(), false);
            }
            if (hash3(x, 11, z) < 0.04) {
                chunk.setBlockState(pos.set(x, bottom, z), Blocks.SCULK_SENSOR.defaultBlockState(), false);
            }
            return;
        }

        boolean ocean = holder.is(BiomeTags.IS_OCEAN);
        boolean desert = biome.getBiomeCategory() == BiomeCategory.DESERT;
        boolean jungle = holder.is(BiomeTags.IS_JUNGLE);
        boolean snowy = biome.coldEnoughToSnow(pos.set(x, bottom, z));

        if (ocean) {
            // 水下残迹：两层水 + 沙底 + 珊瑚海草
            chunk.setBlockState(pos.set(x, bottom, z), hash3(x, 1, z) < 0.3 ? Blocks.GRAVEL.defaultBlockState() : Blocks.SAND.defaultBlockState(), false);
            chunk.setBlockState(pos.set(x, bottom + 1, z), Blocks.WATER.defaultBlockState(), false);
            chunk.setBlockState(pos.set(x, bottom + 2, z), Blocks.WATER.defaultBlockState(), false);
            double r = hash3(x, 2, z);
            if (r < 0.06) chunk.setBlockState(pos.set(x, bottom, z), Blocks.BRAIN_CORAL_BLOCK.defaultBlockState(), false);
            else if (r < 0.14) chunk.setBlockState(pos.set(x, bottom, z), Blocks.SEA_PICKLE.defaultBlockState(), false);
            else if (r < 0.30) chunk.setBlockState(pos.set(x, bottom, z), Blocks.SEAGRASS.defaultBlockState(), false);
        } else if (desert) {
            chunk.setBlockState(pos.set(x, bottom, z), hash3(x, 1, z) < 0.25 ? Blocks.SANDSTONE.defaultBlockState() : Blocks.SAND.defaultBlockState(), false);
            double r = hash3(x, 2, z);
            if (r < 0.05) {
                chunk.setBlockState(pos.set(x, bottom + 1, z), Blocks.DEAD_BUSH.defaultBlockState(), false);
            } else if (r < 0.09) {
                chunk.setBlockState(pos.set(x, bottom + 1, z), Blocks.CACTUS.defaultBlockState(), false);
            }
        } else if (jungle) {
            chunk.setBlockState(pos.set(x, bottom, z), Blocks.MOSS_BLOCK.defaultBlockState(), false);
            double r = hash3(x, 2, z);
            if (r < 0.12) {
                chunk.setBlockState(pos.set(x, bottom + 1, z), Blocks.GRASS.defaultBlockState(), false);
            } else if (r < 0.20) {
                chunk.setBlockState(pos.set(x, bottom + 1, z), Blocks.GLOW_LICHEN.defaultBlockState(), false);
            }
        } else if (snowy) {
            chunk.setBlockState(pos.set(x, bottom, z), hash3(x, 1, z) < 0.3 ? Blocks.ICE.defaultBlockState() : Blocks.SNOW_BLOCK.defaultBlockState(), false);
            if (hash3(x, 2, z) < 0.03) {
                // 地热泉：冒烟篝火
                chunk.setBlockState(pos.set(x, bottom, z),
                    Blocks.CAMPFIRE.defaultBlockState().setValue(net.minecraft.world.level.block.CampfireBlock.LIT, true), false);
            }
        } else {
            chunk.setBlockState(pos.set(x, bottom, z),
                hash3(x, 1, z) < 0.15 ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState(), false);
            if (hash3(x, 2, z) < 0.06) {
                chunk.setBlockState(pos.set(x, bottom + 1, z),
                    hash3(x, 3, z) < 0.5 ? Blocks.GRASS.defaultBlockState() : Blocks.FERN.defaultBlockState(), false);
            }
        }
    }
}
