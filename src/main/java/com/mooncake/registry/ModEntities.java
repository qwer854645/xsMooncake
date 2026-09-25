package com.mooncake.registry;

import com.mooncake.MooncakeMod;
import com.mooncake.entity.HardenedMooncakeArrow;
import com.mooncake.entity.ThrownHardenedMooncake;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MooncakeMod.MOD_ID);

    public static final DeferredHolder<EntityType<?>, EntityType<ThrownHardenedMooncake>> THROWN_HARDENED_MOONCAKE =
            ENTITIES.register("thrown_hardened_mooncake", () -> EntityType.Builder
                    .<ThrownHardenedMooncake>of(ThrownHardenedMooncake::new, MobCategory.MISC)
                    .sized(0.35F, 0.35F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
                    .build(ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "thrown_hardened_mooncake").toString()));

    public static final DeferredHolder<EntityType<?>, EntityType<HardenedMooncakeArrow>> HARDENED_MOONCAKE_ARROW =
            ENTITIES.register("hardened_mooncake_arrow", () -> EntityType.Builder
                    .<HardenedMooncakeArrow>of(HardenedMooncakeArrow::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build(ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "hardened_mooncake_arrow").toString()));

    private ModEntities() {
    }

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}
