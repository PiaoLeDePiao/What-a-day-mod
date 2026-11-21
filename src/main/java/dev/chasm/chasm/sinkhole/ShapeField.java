package dev.chasm.chasm.sinkhole;

import net.minecraft.util.RandomSource;

/**
 * 种子驱动的天坑形状场：r(θ) = 包络(θ) × (1 + 谐波层) × 特征(θ)。
 * 大部分种子谐波相位散乱 -> 轮廓近似圆或纺锤；
 * 少数种子激活"特征分量"（心形/星形/方形模板按强度混合叠加在圆底子上）
 * -> 涌现出酷似特殊形状的异常轮廓，且彼此各不相同。
 * 谐波层参数由 params.seed() 派生（无需序列化），其余全部来自已序列化的 SinkholeParams。
 */
public final class ShapeField {

    private final int featureType;
    private final double featureBlend;
    private final double spindle;
    private final double aspect;
    private final double k2amp, k2phase, k3amp, k3phase, k5amp, k5phase, wobbleAmp, wobblePhase;

    public ShapeField(SinkholeParams p) {
        this.featureType = p.featureType();
        this.featureBlend = p.featureBlend();
        this.spindle = p.spindle();
        this.aspect = p.aspect();
        RandomSource r = RandomSource.create(p.seed() ^ 0x5EED51C7L);
        boolean fixed = p.mode() == SinkholeParams.MODE_FIXED;
        // 中低频谐波：幅度小、相位散乱 -> 一般只带来不规则感，不改变整体轮廓
        this.k2amp = fixed ? 0.0 : r.nextDouble() * 0.10;
        this.k2phase = r.nextDouble() * Math.PI * 2.0;
        this.k3amp = fixed ? 0.0 : r.nextDouble() * 0.07;
        this.k3phase = r.nextDouble() * Math.PI * 2.0;
        this.k5amp = fixed ? 0.0 : r.nextDouble() * 0.05;
        this.k5phase = r.nextDouble() * Math.PI * 2.0;
        this.wobbleAmp = fixed ? 0.0 : 0.04 + r.nextDouble() * 0.06;
        this.wobblePhase = r.nextDouble() * Math.PI * 2.0;
    }

    /**
     * 归一化形状距离：0 = 中心，1 ~ 边缘。
     * u/v 为绕主轴旋转后的坐标（单位：格），u 沿长轴。
     */
    public double scaled(double u, double v) {
        double uu = u;                       // 长轴基准 1
        double vv = v / Math.max(aspect, 0.1); // 短轴
        double theta = Math.atan2(vv, uu);
        double rad = Math.sqrt(uu * uu + vv * vv);

        double envelope = envelopeAt(theta);
        double harmonics = 1.0
            + k2amp * Math.cos(2.0 * theta + k2phase)
            + k3amp * Math.cos(3.0 * theta + k3phase)
            + k5amp * Math.cos(5.0 * theta + k5phase)
            + wobbleAmp * Math.cos(7.0 * theta + wobblePhase);
        double feature = featureAt(theta);

        double r = Math.max(0.04, envelope * harmonics * feature);
        return rad / r;
    }

    /** 基础包络：1 = 圆；spindle > 0 时沿长轴端点收窄（两端尖细、中间较鼓）。 */
    private double envelopeAt(double theta) {
        if (spindle <= 0.0)
            return 1.0;
        double t = Math.abs(Math.cos(theta)); // 长轴分量（1 = 长轴端点）
        double w = 0.22 + 0.78 * Math.sqrt(1.0 - t * t);
        return 1.0 - spindle * (1.0 - w);
    }

    /** 特征分量：无 = 1；方形/心形/星形模板按 featureBlend 强度混合。 */
    private double featureAt(double theta) {
        switch (featureType) {
            case SinkholeParams.FEATURE_SQUARE: {
                // 超椭圆 r(θ)：对角方向伸出（角），轴向为边
                double c = Math.abs(Math.cos(theta));
                double s = Math.abs(Math.sin(theta));
                double sq = Math.cbrt(c * c * c + s * s * s);
                return 1.0 + (1.0 / Math.max(sq, 0.1) - 1.0) * featureBlend;
            }
            case SinkholeParams.FEATURE_HEART: {
                // 心脏线（尖端朝长轴负端），与 v0.2 同源
                double h = 1.0 - 0.72 * Math.sin(theta - Math.PI / 2.0);
                return 1.0 + (h - 1.0) * featureBlend;
            }
            case SinkholeParams.FEATURE_STAR: {
                // 五角星 r = 1 + 0.55 * cos(5θ)
                double st = 1.0 + 0.55 * Math.cos(5.0 * theta);
                return 1.0 + (st - 1.0) * featureBlend;
            }
            default:
                return 1.0;
        }
    }
}
