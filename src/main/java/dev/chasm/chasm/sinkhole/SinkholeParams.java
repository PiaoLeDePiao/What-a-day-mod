package dev.chasm.chasm.sinkhole;

import dev.chasm.chasm.ChasmConfig;
import net.minecraft.util.RandomSource;

/**
 * 单个天坑的全部生成参数，在 findGenerationPoint 中一次性随机确定，
 * 随 piece 序列化保存，保证每个区块重载后雕刻结果一致。
 */
public record SinkholeParams(int mode, int featureType, double featureBlend, double spindle, double aspect,
                             int radiusX, int radiusZ, double rotCos, double rotSin,
                             int depth, int bottomY, long seed) {

    /** 形状模式 */
    public static final int MODE_EMERGENT = 0; // 涌现式：噪声驱动，偶发酷似特殊形状（默认）
    public static final int MODE_FIXED = 1;    // 固定：所有天坑都是选定形状
    public static final int MODE_POOL = 2;     // 旧概率池（v0.2 行为）

    /** 特征模板（涌现混合分量 / 固定形状） */
    public static final int FEATURE_NONE = 0;
    public static final int FEATURE_SQUARE = 1;
    public static final int FEATURE_HEART = 2;
    public static final int FEATURE_STAR = 3;

    /**
     * 采样一组天坑参数。
     * 涌现模式（默认）：形状 = 基础包络(圆/纺锤) × 谐波层 × 特征分量。约 special_chance
     * 概率激活特征分量，以 feature_blend 强度混合，产生酷似心形/星形/方形的异常轮廓。
     * 固定模式：所有天坑都是 fixed_shape 所选形状。
     * 注意：MC 结构系统只在结构中心 ±8 区块内生成内容，因此长轴被硬限制为 16 区块。
     */
    public static SinkholeParams sample(RandomSource random, int surfaceY, int minBuildHeight) {
        int minChunks = ChasmConfig.SINKHOLE_MIN_CHUNKS.get();
        int maxChunks = Math.min(ChasmConfig.SINKHOLE_MAX_CHUNKS.get(), 16);

        // 尺寸：重尾分布（大多数接近下限，偶有大坑）
        double t = Math.pow(random.nextDouble(), ChasmConfig.SINKHOLE_SIZE_EXPONENT.get());
        int lengthChunks = minChunks + (int) Math.round((maxChunks - minChunks) * t);
        if (random.nextDouble() < ChasmConfig.SINKHOLE_HUGE_CHANCE.get()) {
            double mult = ChasmConfig.SINKHOLE_HUGE_MULT_MIN.get()
                + random.nextDouble() * (ChasmConfig.SINKHOLE_HUGE_MULT_MAX.get() - ChasmConfig.SINKHOLE_HUGE_MULT_MIN.get());
            lengthChunks = (int) Math.min(16L, Math.round(lengthChunks * mult));
        }

        int radiusX = lengthChunks * 8; // 长轴半径（格）

        // ---- 形状 ----
        int mode;
        switch (ChasmConfig.SINKHOLE_SHAPE_MODE.get()) {
            case "fixed" -> mode = MODE_FIXED;
            case "pool" -> mode = MODE_POOL;
            default -> mode = MODE_EMERGENT;
        }

        int featureType = FEATURE_NONE;
        double featureBlend = 0.0;
        double spindle = 0.0;
        double aspect = 1.0;

        switch (mode) {
            case MODE_FIXED -> {
                switch (ChasmConfig.SINKHOLE_FIXED_SHAPE.get()) {
                    case "spindle" -> {
                        spindle = 1.0;
                        aspect = 0.30 + random.nextDouble() * 0.10;
                    }
                    case "square" -> {
                        featureType = FEATURE_SQUARE;
                        featureBlend = 1.0;
                    }
                    case "heart" -> {
                        featureType = FEATURE_HEART;
                        featureBlend = 1.0;
                    }
                    case "star" -> {
                        featureType = FEATURE_STAR;
                        featureBlend = 1.0;
                    }
                    default -> {
                        // circle：保持默认
                    }
                }
            }
            case MODE_POOL -> {
                if (random.nextDouble() < ChasmConfig.SINKHOLE_SPECIAL_CHANCE.get()) {
                    featureType = 1 + random.nextInt(3);
                    featureBlend = 1.0;
                } else if (random.nextDouble() < ChasmConfig.SINKHOLE_SPINDLE_CHANCE.get()) {
                    spindle = 1.0;
                    aspect = 0.30 + random.nextDouble() * 0.10;
                }
            }
            default -> { // MODE_EMERGENT
                if (random.nextDouble() < ChasmConfig.SINKHOLE_SPECIAL_CHANCE.get()) {
                    // 特征分量激活：以中等强度混合到圆底子上 -> 涌现各种"半心形/歪星形/类方形"
                    featureType = 1 + random.nextInt(3);
                    featureBlend = ChasmConfig.SINKHOLE_FEATURE_BLEND_MIN.get()
                        + random.nextDouble() * (ChasmConfig.SINKHOLE_FEATURE_BLEND_MAX.get()
                            - ChasmConfig.SINKHOLE_FEATURE_BLEND_MIN.get());
                    spindle = random.nextDouble() < 0.3 ? 0.25 + random.nextDouble() * 0.25 : 0.0;
                } else {
                    spindle = random.nextDouble() < ChasmConfig.SINKHOLE_SPINDLE_CHANCE.get() ? 1.0 : 0.0;
                }
                if (spindle > 0.0)
                    aspect = 0.30 + random.nextDouble() * 0.10;
            }
        }

        int radiusZ = (int) Math.max(8, Math.round(radiusX * aspect));

        // 主轴随机旋转
        double angle = random.nextDouble() * Math.PI * 2;

        // 深度
        int depth = ChasmConfig.SINKHOLE_DEPTH_MIN.get()
            + random.nextInt(ChasmConfig.SINKHOLE_DEPTH_MAX.get() - ChasmConfig.SINKHOLE_DEPTH_MIN.get() + 1);
        int bottomY;
        if (random.nextDouble() < ChasmConfig.SINKHOLE_BEDROCK_CHANCE.get()) {
            bottomY = minBuildHeight + 8; // 直达基岩上方
        } else {
            bottomY = surfaceY - depth;
        }
        if (bottomY < minBuildHeight + 4)
            bottomY = minBuildHeight + 4;

        return new SinkholeParams(mode, featureType, featureBlend, spindle, aspect,
            radiusX, radiusZ, Math.cos(angle), Math.sin(angle), depth, bottomY, random.nextLong());
    }
}
