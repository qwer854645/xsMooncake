package com.mooncake.registry;

import com.mooncake.MooncakeMod;
import com.mooncake.component.MooncakeCrust;
import com.mooncake.component.MooncakeFillings;
import com.mooncake.component.MooncakeInscription;
import com.mooncake.component.MooncakeWeather;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModComponents {
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MooncakeMod.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MooncakeFillings>> FILLINGS =
            COMPONENTS.registerComponentType("fillings", (UnaryOperator<DataComponentType.Builder<MooncakeFillings>>) builder -> builder
                    .persistent(MooncakeFillings.CODEC)
                    .networkSynchronized(MooncakeFillings.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MooncakeCrust>> CRUST =
            COMPONENTS.registerComponentType("crust", (UnaryOperator<DataComponentType.Builder<MooncakeCrust>>) builder -> builder
                    .persistent(MooncakeCrust.CODEC)
                    .networkSynchronized(MooncakeCrust.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MooncakeWeather>> WEATHER =
            COMPONENTS.registerComponentType("weather", (UnaryOperator<DataComponentType.Builder<MooncakeWeather>>) builder -> builder
                    .persistent(MooncakeWeather.CODEC)
                    .networkSynchronized(MooncakeWeather.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<MooncakeInscription>> INSCRIPTION =
            COMPONENTS.registerComponentType("inscription", (UnaryOperator<DataComponentType.Builder<MooncakeInscription>>) builder -> builder
                    .persistent(MooncakeInscription.CODEC)
                    .networkSynchronized(MooncakeInscription.STREAM_CODEC));

    private ModComponents() {
    }

    public static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}
