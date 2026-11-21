package dev.chasm.chasm.mount;

import dev.chasm.chasm.Chasm;
import dev.chasm.chasm.ChasmConfig;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** FR-1：坐骑速度上限提升。实体加入世界（出生/区块加载）时按配置加乘算修饰器。 */
@EventBusSubscriber(modid = Chasm.MODID)
public class MountSpeedHandler {

    private static final Map<EntityType<?>, UUID> UUIDS = Map.of(
        EntityType.HORSE, UUID.fromString("0c0a5e0d-0000-4000-8000-000000000001"),
        EntityType.DONKEY, UUID.fromString("0c0a5e0d-0000-4000-8000-000000000002"),
        EntityType.MULE, UUID.fromString("0c0a5e0d-0000-4000-8000-000000000003"),
        EntityType.CAMEL, UUID.fromString("0c0a5e0d-0000-4000-8000-000000000004"),
        EntityType.LLAMA, UUID.fromString("0c0a5e0d-0000-4000-8000-000000000005"),
        EntityType.TRADER_LLAMA, UUID.fromString("0c0a5e0d-0000-4000-8000-000000000006"),
        EntityType.PIG, UUID.fromString("0c0a5e0d-0000-4000-8000-000000000007"),
        EntityType.STRIDER, UUID.fromString("0c0a5e0d-0000-4000-8000-000000000008"),
        EntityType.SKELETON_HORSE, UUID.fromString("0c0a5e0d-0000-4000-8000-000000000009"),
        EntityType.ZOMBIE_HORSE, UUID.fromString("0c0a5e0d-0000-4000-8000-00000000000a")
    );

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide())
            return;
        if (!ChasmConfig.ENABLE_MOUNTS.get())
            return;
        if (!(event.getEntity() instanceof LivingEntity living))
            return;

        EntityType<?> type = living.getType();
        UUID uuid = UUIDS.get(type);
        if (uuid == null)
            return;

        double multiplier = multiplierFor(type);
        if (multiplier <= 0.0)
            return;

        AttributeInstance speed = living.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null)
            return;
        // amount = 倍率 - 1（add_multiplied_total 会把总值乘上 1+amount）
        speed.addPermanentModifier(new AttributeModifier(uuid, "chasm:mount_speed",
            multiplier - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    private static double multiplierFor(EntityType<?> type) {
        if (type == EntityType.HORSE) return ChasmConfig.MOUNT_HORSE.get();
        if (type == EntityType.DONKEY) return ChasmConfig.MOUNT_DONKEY.get();
        if (type == EntityType.MULE) return ChasmConfig.MOUNT_MULE.get();
        if (type == EntityType.CAMEL) return ChasmConfig.MOUNT_CAMEL.get();
        if (type == EntityType.LLAMA) return ChasmConfig.MOUNT_LLAMA.get();
        if (type == EntityType.TRADER_LLAMA) return ChasmConfig.MOUNT_TRADER_LLAMA.get();
        if (type == EntityType.PIG) return ChasmConfig.MOUNT_PIG.get();
        if (type == EntityType.STRIDER) return ChasmConfig.MOUNT_STRIDER.get();
        if (type == EntityType.SKELETON_HORSE) return ChasmConfig.MOUNT_SKELETON_HORSE.get();
        if (type == EntityType.ZOMBIE_HORSE) return ChasmConfig.MOUNT_ZOMBIE_HORSE.get();
        return 0.0;
    }
}
