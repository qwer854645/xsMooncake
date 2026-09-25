package com.mooncake.registry;

import com.mooncake.MooncakeMod;
import java.util.EnumMap;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.WeatheringCopper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * One armor material per spoil stage so worn layers match hardened cake colors.
 * Waxed gear reuses the same stage material as its unwaxed twin.
 */
public final class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, MooncakeMod.MOD_ID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> HARDENED_MOONCAKE =
            register("hardened_mooncake");
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> EXPOSED_HARDENED_MOONCAKE =
            register("exposed_hardened_mooncake");
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> WEATHERED_HARDENED_MOONCAKE =
            register("weathered_hardened_mooncake");
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> OXIDIZED_HARDENED_MOONCAKE =
            register("oxidized_hardened_mooncake");

    private ModArmorMaterials() {
    }

    private static DeferredHolder<ArmorMaterial, ArmorMaterial> register(String layerName) {
        return ARMOR_MATERIALS.register(
                layerName,
                () -> new ArmorMaterial(
                        Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                            map.put(ArmorItem.Type.BOOTS, 2);
                            map.put(ArmorItem.Type.LEGGINGS, 5);
                            map.put(ArmorItem.Type.CHESTPLATE, 6);
                            map.put(ArmorItem.Type.HELMET, 2);
                            map.put(ArmorItem.Type.BODY, 5);
                        }),
                        12,
                        SoundEvents.ARMOR_EQUIP_IRON,
                        () -> Ingredient.of(ModTags.HARDENED_MOONCAKE_CAKES),
                        List.of(new ArmorMaterial.Layer(
                                ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, layerName))),
                        0.5F,
                        0.0F
                )
        );
    }

    public static Holder<ArmorMaterial> forState(WeatheringCopper.WeatherState state) {
        return switch (state) {
            case UNAFFECTED -> HARDENED_MOONCAKE;
            case EXPOSED -> EXPOSED_HARDENED_MOONCAKE;
            case WEATHERED -> WEATHERED_HARDENED_MOONCAKE;
            case OXIDIZED -> OXIDIZED_HARDENED_MOONCAKE;
        };
    }

    /** @deprecated use {@link #forState(WeatheringCopper.WeatherState)} */
    @Deprecated
    public static Holder<ArmorMaterial> hardened() {
        return HARDENED_MOONCAKE;
    }

    public static void register(IEventBus bus) {
        ARMOR_MATERIALS.register(bus);
    }
}
