package dev.chasm.chasm.sinkhole;

import com.mojang.serialization.MapCodec;
import dev.chasm.chasm.Chasm;
import dev.chasm.chasm.ChasmConfig;
import java.util.List;
import java.util.Map;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** FR-4：天坑结构的注册（结构类型、结构、piece 类型、结构集）。 */
public class ChasmStructures {

    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES =
        DeferredRegister.create(Registries.STRUCTURE_TYPE, Chasm.MODID);
    public static final DeferredRegister<Structure> STRUCTURES =
        DeferredRegister.create(Registries.STRUCTURE, Chasm.MODID);
    public static final DeferredRegister<StructurePieceType> PIECE_TYPES =
        DeferredRegister.create(Registries.STRUCTURE_PIECE, Chasm.MODID);
    public static final DeferredRegister<StructureSet> STRUCTURE_SETS =
        DeferredRegister.create(Registries.STRUCTURE_SET, Chasm.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<SinkholeStructure>> SINKHOLE_TYPE =
        STRUCTURE_TYPES.register("sinkhole", () -> new StructureType<>() {
            public String getSerializedName() {
                return "chasm:sinkhole";
            }

            @Override
            public MapCodec<SinkholeStructure> codec() {
                return SinkholeStructure.CODEC;
            }
        });

    public static final DeferredHolder<Structure, SinkholeStructure> SINKHOLE =
        STRUCTURES.register("sinkhole", () -> new SinkholeStructure(
            new Structure.StructureSettings(allOverworldBiomes(), Map.of(),
                GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE)));

    public static final DeferredHolder<StructurePieceType, StructurePieceType> SINKHOLE_PIECE =
        PIECE_TYPES.register("sinkhole_piece", () -> (ctx, tag) -> new SinkholePiece(ctx, tag));

    public static final DeferredHolder<StructureSet, StructureSet> SINKHOLE_SET =
        STRUCTURE_SETS.register("sinkhole", () -> new StructureSet(
            List.of(new StructureSet.StructureSelectionEntry(SINKHOLE, 1)),
            new RandomSpreadStructurePlacement(
                ChasmConfig.SINKHOLE_SPACING.get(),
                ChasmConfig.SINKHOLE_SEPARATION.get(),
                RandomSpreadType.LINEAR, 1526052012)));

    private static HolderSet<Biome> allOverworldBiomes() {
        return HolderSet.direct(); // 空集合 = 不限制群系，具体群系由生成点检查控制
    }
}
