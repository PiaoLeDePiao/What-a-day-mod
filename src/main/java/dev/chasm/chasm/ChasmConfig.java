package dev.chasm.chasm;

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.List;


/** 全部功能的 TOML 配置（config/chasm.toml），带中文注释。 */
public class ChasmConfig {

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLE_MOUNTS;
    public static final ModConfigSpec.DoubleValue MOUNT_HORSE;
    public static final ModConfigSpec.DoubleValue MOUNT_DONKEY;
    public static final ModConfigSpec.DoubleValue MOUNT_MULE;
    public static final ModConfigSpec.DoubleValue MOUNT_CAMEL;
    public static final ModConfigSpec.DoubleValue MOUNT_LLAMA;
    public static final ModConfigSpec.DoubleValue MOUNT_TRADER_LLAMA;
    public static final ModConfigSpec.DoubleValue MOUNT_PIG;
    public static final ModConfigSpec.DoubleValue MOUNT_STRIDER;
    public static final ModConfigSpec.DoubleValue MOUNT_SKELETON_HORSE;
    public static final ModConfigSpec.DoubleValue MOUNT_ZOMBIE_HORSE;

    public static final ModConfigSpec.BooleanValue ENABLE_GLUE;
    public static final ModConfigSpec.IntValue GLUE_MAX_DAMAGE;
    public static final ModConfigSpec.IntValue GLUE_REACH_BONUS;

    public static final ModConfigSpec.BooleanValue ENABLE_POTION;
    public static final ModConfigSpec.IntValue FLIGHT_DURATION_TICKS;
    public static final ModConfigSpec.IntValue FLIGHT_LONG_DURATION_TICKS;

    public static final ModConfigSpec.BooleanValue ENABLE_SINKHOLE;
    public static final ModConfigSpec.IntValue SINKHOLE_SPACING;
    public static final ModConfigSpec.IntValue SINKHOLE_SEPARATION;
    public static final ModConfigSpec.IntValue SINKHOLE_MIN_CHUNKS;
    public static final ModConfigSpec.IntValue SINKHOLE_MAX_CHUNKS;
    public static final ModConfigSpec.DoubleValue SINKHOLE_SIZE_EXPONENT;
    public static final ModConfigSpec.IntValue SINKHOLE_HUGE_THRESHOLD;
    public static final ModConfigSpec.IntValue SINKHOLE_HUGE_GAP_CHUNKS;
    public static final ModConfigSpec.DoubleValue SINKHOLE_HUGE_CHANCE;
    public static final ModConfigSpec.DoubleValue SINKHOLE_HUGE_MULT_MIN;
    public static final ModConfigSpec.DoubleValue SINKHOLE_HUGE_MULT_MAX;
    public static final ModConfigSpec.IntValue SINKHOLE_DEPTH_MIN;
    public static final ModConfigSpec.IntValue SINKHOLE_DEPTH_MAX;
    public static final ModConfigSpec.DoubleValue SINKHOLE_BEDROCK_CHANCE;
    public static final ModConfigSpec.DoubleValue SINKHOLE_OCEAN_WEIGHT;
    public static final ModConfigSpec.IntValue SINKHOLE_SPAWN_PROTECTION_CHUNKS;
    public static final ModConfigSpec.BooleanValue SINKHOLE_WALL_STRATA;
    public static final ModConfigSpec.BooleanValue SINKHOLE_BOTTOM_ECOLOGY;
    public static final ModConfigSpec.DoubleValue SINKHOLE_LAVA_SEA_CHANCE;
    public static final ModConfigSpec.DoubleValue SINKHOLE_SCULK_CHANCE;
    public static final ModConfigSpec.DoubleValue SINKHOLE_WATERFALL_CHANCE;
    public static final ModConfigSpec.DoubleValue SINKHOLE_VINE_CHANCE;
    public static final ModConfigSpec.DoubleValue SINKHOLE_SAND_PILLAR_CHANCE;
    public static final ModConfigSpec.DoubleValue SINKHOLE_DEBRIS_EDGE_CHANCE;
    public static final ModConfigSpec.DoubleValue SINKHOLE_RUINS_CHANCE;
    public static final ModConfigSpec.DoubleValue SINKHOLE_SPECIAL_CHANCE;
    public static final ModConfigSpec.DoubleValue SINKHOLE_SPINDLE_CHANCE;
    public static final ModConfigSpec.ConfigValue<String> SINKHOLE_SHAPE_MODE;
    public static final ModConfigSpec.ConfigValue<String> SINKHOLE_FIXED_SHAPE;
    public static final ModConfigSpec.DoubleValue SINKHOLE_FEATURE_BLEND_MIN;
    public static final ModConfigSpec.DoubleValue SINKHOLE_FEATURE_BLEND_MAX;
    public static final ModConfigSpec.BooleanValue SINKHOLE_STRUCTURES_ENABLED;
    public static final ModConfigSpec.IntValue SINKHOLE_STRUCTURES_MAX;
    public static final ModConfigSpec.DoubleValue SINKHOLE_STRUCTURES_VILLAGE_CHANCE;
    public static final ModConfigSpec.IntValue SINKHOLE_STRUCTURES_VILLAGE_MIN_RADIUS;
    public static final ModConfigSpec.DoubleValue SINKHOLE_STRUCTURES_ANCIENT_CITY_CHANCE;
    public static final ModConfigSpec.IntValue SINKHOLE_STRUCTURES_ANCIENT_CITY_MIN_DEPTH;
    public static final ModConfigSpec.DoubleValue SINKHOLE_STRUCTURES_UNDERWATER_CHANCE;

    public static final ModConfigSpec.BooleanValue ENABLE_MUSIC;
    public static final ModConfigSpec.DoubleValue MUSIC_VOLUME;
    public static final ModConfigSpec.DoubleValue MUSIC_CHANCE;
    public static final ModConfigSpec.IntValue MUSIC_MIN_DELAY_SECONDS;
    public static final ModConfigSpec.IntValue MUSIC_MAX_DELAY_SECONDS;
    public static final ModConfigSpec.BooleanValue MUSIC_OVERLAP_VANILLA;

    public static final ModConfigSpec.BooleanValue ENABLE_FIREWORKS;
    public static final ModConfigSpec.BooleanValue FIREWORKS_BOOST_ENABLED;
    public static final ModConfigSpec.DoubleValue FIREWORKS_BOOST_STRENGTH;
    public static final ModConfigSpec.BooleanValue FIREWORKS_ENDLESS_ENABLED;
    public static final ModConfigSpec.DoubleValue FIREWORKS_ENDLESS_THRUST;
    public static final ModConfigSpec.DoubleValue FIREWORKS_MAX_SPEED;

    public static final ModConfigSpec.BooleanValue ENABLE_HARVEST;
    public static final ModConfigSpec.DoubleValue HARVEST_DIAMOND_CHANCE;
    public static final ModConfigSpec.DoubleValue HARVEST_NETHERITE_CHANCE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> HARVEST_EXTRA_CROPS;

    static {
        SPEC = build();


    }

    private static ModConfigSpec build() {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("mounts");
        b.comment("坐骑速度：倍率表示最终速度 = 原速度 × 倍率（1.0 = 不变，0 = 关闭该坐骑）。修改后重新进入世界生效。");
        ENABLE_MOUNTS = b.comment("总开关").define("enable", true);
        MOUNT_HORSE = b.defineInRange("horse", 2.3, 0.0, 1000.0);
        MOUNT_DONKEY = b.defineInRange("donkey", 1.9, 0.0, 1000.0);
        MOUNT_MULE = b.defineInRange("mule", 1.5, 0.0, 1000.0);
        MOUNT_CAMEL = b.defineInRange("camel", 1.5, 0.0, 1000.0);
        MOUNT_LLAMA = b.defineInRange("llama", 1.7, 0.0, 1000.0);
        MOUNT_TRADER_LLAMA = b.defineInRange("trader_llama", 1.1, 0.0, 1000.0);
        MOUNT_PIG = b.defineInRange("pig", 1.5, 0.0, 1000.0);
        MOUNT_STRIDER = b.defineInRange("strider", 3.0, 0.0, 1000.0);
        MOUNT_SKELETON_HORSE = b.defineInRange("skeleton_horse", 2.0, 0.0, 1000.0);
        MOUNT_ZOMBIE_HORSE = b.defineInRange("zombie_horse", 2.0, 0.0, 1000.0);
        b.pop();

        b.push("glue");
        b.comment("强力胶（机械动力 create:super_glue）强化。");
        ENABLE_GLUE = b.comment("总开关").define("enable", true);
        GLUE_MAX_DAMAGE = b.comment("耐久上限（默认 2147483647 ≈ 无限）").defineInRange("max_damage", 2147483647, 1, 2147483647);
        GLUE_REACH_BONUS = b.comment("使用距离加成（格）。持有强力胶时玩家的方块交互范围额外增加这么多格，默认 2000 约等于无上限").defineInRange("reach_bonus", 2000, 0, 100000);
        b.pop();

        b.push("potion");
        b.comment("飞行药水。");
        ENABLE_POTION = b.comment("总开关").define("enable", true);
        FLIGHT_DURATION_TICKS = b.comment("飞行药水基础时长（刻，默认 36000 = 30 分钟）").defineInRange("flight_duration_ticks", 36000, 1, Integer.MAX_VALUE);
        FLIGHT_LONG_DURATION_TICKS = b.comment("红石延长版时长（刻，默认 144000 = 2 小时）").defineInRange("long_duration_ticks", 144000, 1, Integer.MAX_VALUE);
        b.pop();

        b.push("sinkhole");
        b.comment("天坑结构生成。频率与远古城市同级或更低：spacing 越大越稀疏。改配置只影响之后探索的新区域。");
        ENABLE_SINKHOLE = b.comment("总开关").define("enable", true);
        SINKHOLE_SPACING = b.comment("结构网格间距（区块），默认 8").defineInRange("spacing", 8, 3, 64);
        SINKHOLE_SEPARATION = b.comment("同一网格内两个天坑的最小间距（区块），默认 5").defineInRange("separation", 5, 0, 32);
        SINKHOLE_MIN_CHUNKS = b.comment("天坑最短轴下限（区块），默认 6").defineInRange("min_chunks", 6, 3, 256);
        SINKHOLE_MAX_CHUNKS = b.comment("天坑最长轴常规上限（区块），默认 24").defineInRange("max_chunks", 24, 6, 1024);
        SINKHOLE_SIZE_EXPONENT = b.comment("尺寸分布指数（越大越偏向小坑；3.0 时绝大多数接近下限，偶有巨坑）").defineInRange("size_exponent", 3.0, 0.5, 10.0);
        SINKHOLE_HUGE_THRESHOLD = b.comment("超过该轴长（区块）视为特大坑").defineInRange("huge_threshold", 16, 6, 1024);
        SINKHOLE_HUGE_GAP_CHUNKS = b.comment("特大坑之间最小距离（区块），避免两个巨坑连在一起").defineInRange("huge_gap_chunks", 24, 8, 4096);
        SINKHOLE_HUGE_CHANCE = b.comment("超大型天坑额外概率（在常规尺寸之外）").defineInRange("huge_chance", 0.02, 0.0, 1.0);
        SINKHOLE_HUGE_MULT_MIN = b.comment("超大型倍率下限").defineInRange("huge_mult_min", 3.0, 1.0, 100.0);
        SINKHOLE_HUGE_MULT_MAX = b.comment("超大型倍率上限（理论上限可配到数千区块，概率极低）").defineInRange("huge_mult_max", 8.0, 1.0, 100.0);
        SINKHOLE_DEPTH_MIN = b.comment("天坑最小深度（格）").defineInRange("depth_min", 40, 10, 200);
        SINKHOLE_DEPTH_MAX = b.comment("天坑最大深度（格）").defineInRange("depth_max", 90, 10, 300);
        SINKHOLE_BEDROCK_CHANCE = b.comment("直达基岩上方的概率").defineInRange("bedrock_chance", 0.005, 0.0, 1.0);
        SINKHOLE_OCEAN_WEIGHT = b.comment("海洋/大水域群系生成权重（陆地=1.0）").defineInRange("ocean_weight", 3.0, 0.0, 100.0);
        SINKHOLE_SPAWN_PROTECTION_CHUNKS = b.comment("出生点保护半径（区块，默认 100 = 1600 格）").defineInRange("spawn_protection_chunks", 100, 0, 10000);
        SINKHOLE_WALL_STRATA = b.comment("崖壁条带纹理").define("wall_strata", true);
        SINKHOLE_BOTTOM_ECOLOGY = b.comment("底部生态").define("bottom_ecology", true);
        SINKHOLE_LAVA_SEA_CHANCE = b.comment("底部为熔岩海的概率").defineInRange("lava_sea_chance", 0.25, 0.0, 1.0);
        SINKHOLE_SCULK_CHANCE = b.comment("底部为幽匿深暗风格的概率").defineInRange("sculk_chance", 0.15, 0.0, 1.0);
        SINKHOLE_WATERFALL_CHANCE = b.comment("崖顶水瀑概率").defineInRange("waterfall_chance", 0.6, 0.0, 1.0);
        SINKHOLE_VINE_CHANCE = b.comment("崖壁藤蔓概率").defineInRange("vine_chance", 0.5, 0.0, 1.0);
        SINKHOLE_SAND_PILLAR_CHANCE = b.comment("沙漠沙柱倒挂概率").defineInRange("sand_pillar_chance", 0.5, 0.0, 1.0);
        SINKHOLE_DEBRIS_EDGE_CHANCE = b.comment("边缘碎石断层概率").defineInRange("debris_edge_chance", 0.7, 0.0, 1.0);
        SINKHOLE_RUINS_CHANCE = b.comment("崖壁悬空残骸房概率").defineInRange("ruins_chance", 0.35, 0.0, 1.0);
        SINKHOLE_SPECIAL_CHANCE = b.comment("特殊形状（方形/心形/星形）总概率，其余为基础形状（圆/纺锤）").defineInRange("special_chance", 0.10, 0.0, 1.0);
        SINKHOLE_SPINDLE_CHANCE = b.comment("基础形状中纺锤（裂口）型的概率，其余为圆形").defineInRange("spindle_chance", 0.45, 0.0, 1.0);
        SINKHOLE_SHAPE_MODE = b.comment("形状模式：emergent=涌现式（推荐，噪声驱动偶发酷似心形/星形等特殊轮廓）/ fixed=固定所选形状 / pool=旧概率池").define("shape_mode", "emergent");
        SINKHOLE_FIXED_SHAPE = b.comment("shape_mode=fixed 时的形状：circle/spindle/square/heart/star").define("fixed_shape", "star");
        SINKHOLE_FEATURE_BLEND_MIN = b.comment("涌现模式下特征分量（心形/星形/方形）的最小混合强度").defineInRange("feature_blend_min", 0.45, 0.0, 1.0);
        SINKHOLE_FEATURE_BLEND_MAX = b.comment("涌现模式下特征分量的最大混合强度（越大轮廓越接近模板形状）").defineInRange("feature_blend_max", 0.80, 0.0, 1.0);
        b.pop();

        b.push("sinkhole_structures");
        b.comment("天坑结构联动：大小、深度、内容物符合条件的坑底概率生成原版结构。");
        SINKHOLE_STRUCTURES_ENABLED = b.comment("总开关").define("enable", true);
        SINKHOLE_STRUCTURES_MAX = b.comment("单个天坑内最多放置几个联动结构").defineInRange("max_structures", 1, 0, 4);
        SINKHOLE_STRUCTURES_VILLAGE_CHANCE = b.comment("符合条件的坑底生成村庄遗迹的概率（废弃村庄：房屋与枯灌木，无村民）").defineInRange("village_chance", 0.18, 0.0, 1.0);
        SINKHOLE_STRUCTURES_VILLAGE_MIN_RADIUS = b.comment("生成村庄所需的最小天坑长轴半径（格）").defineInRange("village_min_radius", 12, 8, 512);
        SINKHOLE_STRUCTURES_ANCIENT_CITY_CHANCE = b.comment("深坑底部生成远古城市中心的概率").defineInRange("ancient_city_chance", 0.12, 0.0, 1.0);
        SINKHOLE_STRUCTURES_ANCIENT_CITY_MIN_DEPTH = b.comment("生成远古城市所需的最小坑深（格）").defineInRange("ancient_city_min_depth", 40, 10, 200);
        SINKHOLE_STRUCTURES_UNDERWATER_CHANCE = b.comment("海洋天坑底部生成水下遗迹的概率").defineInRange("underwater_ruin_chance", 0.30, 0.0, 1.0);
        b.pop();
        b.pop();

        b.push("music");
        b.comment("背景音乐：把 mp3 放进 config/chasm/music/ 文件夹即可（随机插入式，与 NetMusic 共存）。");
        ENABLE_MUSIC = b.comment("总开关").define("enable", true);
        MUSIC_VOLUME = b.comment("音量（0~1，最终音量还会乘上游戏音乐音量）").defineInRange("volume", 0.6, 0.0, 1.0);
        MUSIC_CHANCE = b.comment("每个检查周期（约 20 秒一次）触发播放的概率").defineInRange("chance", 0.15, 0.0, 1.0);
        MUSIC_MIN_DELAY_SECONDS = b.comment("两首歌之间的最小间隔（秒）").defineInRange("min_delay_seconds", 300, 10, 86400);
        MUSIC_MAX_DELAY_SECONDS = b.comment("两首歌之间的最大间隔（秒）").defineInRange("max_delay_seconds", 900, 10, 86400);
        MUSIC_OVERLAP_VANILLA = b.comment("原版音乐正在播放时是否也插播（false=不打扰原版）").define("overlap_vanilla", false);
        b.pop();

        b.push("fireworks");
        b.comment("烟花强化：1纸+1~8火药合成长效烟花、boost_level 加速配方、endless_boost 无限附魔。");
        ENABLE_FIREWORKS = b.comment("烟花强化总开关（同时控制烟花能否附魔）").define("enable", true);
        FIREWORKS_BOOST_ENABLED = b.comment("boost_level 烟花加速是否生效").define("boost_enabled", true);
        FIREWORKS_BOOST_STRENGTH = b.comment("每级 boost_level 的瞬时加速强度（飞行方向上叠加的速度，格/tick）").defineInRange("boost_strength", 0.12, 0.0, 2.0);
        FIREWORKS_ENDLESS_ENABLED = b.comment("无限附魔（endless_boost）持续推进是否生效").define("endless_enabled", true);
        FIREWORKS_ENDLESS_THRUST = b.comment("无限附魔每次补充的推力（格/tick）").defineInRange("endless_thrust", 0.04, 0.0, 1.0);
        FIREWORKS_MAX_SPEED = b.comment("鞘翅加速水平速度上限（格/tick，原版约 1.8~2.0）").defineInRange("max_speed", 2.2, 0.5, 8.0);
        b.pop();

        b.push("harvest");
        b.comment("作物收割惊喜掉落：收割作物时极低概率额外掉落钻石/下界合金锭。");
        ENABLE_HARVEST = b.comment("总开关").define("enable", true);
        HARVEST_DIAMOND_CHANCE = b.comment("收割作物时掉落钻石的概率（0.001=0.1%，0.01=1%）").defineInRange("diamond_chance", 0.002, 0.0, 1.0);
        HARVEST_NETHERITE_CHANCE = b.comment("收割作物时掉落下界合金锭的概率").defineInRange("netherite_chance", 0.001, 0.0, 1.0);
        HARVEST_EXTRA_CROPS = b.comment("额外视为作物的方块列表（默认已包含 #minecraft:crops 标签内的作物）").defineList("extra_crops", List.of("minecraft:sugar_cane", "minecraft:cactus", "minecraft:nether_wart", "minecraft:cocoa"), o -> o instanceof String);
        b.pop();

        return b.build();
    }

    public static void init() {
        ModLoadingContext.get().registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, SPEC);
    }
}
