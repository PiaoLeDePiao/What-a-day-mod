package dev.chasm.chasm;

import dev.chasm.chasm.glue.GlueHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

/**
 * 物品"默认组件"修改。此事件在注册阶段派发、早于配置文件加载，
 * 因此这里只允许使用硬编码常量；运行期按配置的同步见 GlueHandler 的 tick 逻辑。
 */
public class ChasmDefaults {

    /** 强力胶默认耐久上限（运行期按配置再同步）。 */
    public static final int GLUE_DEFAULT_MAX_DAMAGE = 2147483647;

    /** 在 mod 总线上调用（见 Chasm 主类）。 */
    public static void modifyDefaultComponents(ModifyDefaultComponentsEvent event) {
        // 强力胶：默认耐久接近无限
        Item glue = BuiltInRegistries.ITEM.get(GlueHandler.SUPER_GLUE_ID);
        if (glue != null && glue != Items.AIR) {
            event.modify(glue, builder -> builder.set(DataComponents.MAX_DAMAGE, GLUE_DEFAULT_MAX_DAMAGE));
        }
        // 烟花：默认可附魔（值 1），配合数据驱动的 chasm:endless_boost 附魔
        event.modify(Items.FIREWORK_ROCKET, builder -> builder.set(DataComponents.ENCHANTABLE, 1));
    }
}
