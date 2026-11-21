package dev.chasm.chasm;

import com.mojang.serialization.Codec;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ChasmDataComponents {

    public static final DeferredRegister.DataComponents REGISTRY = DeferredRegister.createDataComponents(Chasm.MODID);

    /**
     * Firework extra elytra-boost level (1..8). Higher = stronger speed kick.
     */
    public static final Supplier<DataComponentType<Byte>> BOOST_LEVEL =
            REGISTRY.registerComponentType("boost_level",
                    builder -> builder.persistent(Codec.BYTE).networkSynchronized(ByteBufCodecs.BYTE));

    private ChasmDataComponents() {
    }
}
