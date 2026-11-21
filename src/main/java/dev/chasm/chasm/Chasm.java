package dev.chasm.chasm;

import dev.chasm.chasm.firework.FireworkBooster;
import dev.chasm.chasm.harvest.HarvestHandler;
import dev.chasm.chasm.glue.GlueHandler;
import dev.chasm.chasm.potion.ChasmPotions;
import dev.chasm.chasm.recipe.ChasmRecipes;
import dev.chasm.chasm.sinkhole.ChasmStructures;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Chasm.MODID)
public class Chasm {
    public static final String MODID = "chasm";

    public Chasm(IEventBus modBus) {
        ChasmConfig.init();
        // 强力胶：修改 Create 物品的默认组件（耐久）
        modBus.addListener(GlueHandler::modifyDefaultComponents);
        // 飞行药水：效果、药水、酿造配方
        ChasmPotions.MOB_EFFECTS.register(modBus);
        ChasmPotions.POTIONS.register(modBus);
        modBus.addListener(ChasmPotions::onCommonSetup);
        // 天坑结构
        ChasmStructures.STRUCTURE_TYPES.register(modBus);
        ChasmStructures.PIECE_TYPES.register(modBus);
        ChasmStructures.STRUCTURES.register(modBus);
        ChasmStructures.STRUCTURE_SETS.register(modBus);
        // 烟花强化：数据组件、自定义配方、使用逻辑
        ChasmDataComponents.REGISTRY.register(modBus);
        ChasmRecipes.RECIPE_SERIALIZERS.register(modBus);
        NeoForge.EVENT_BUS.register(FireworkBooster.class);
        NeoForge.EVENT_BUS.register(HarvestHandler.class);
    }
}
