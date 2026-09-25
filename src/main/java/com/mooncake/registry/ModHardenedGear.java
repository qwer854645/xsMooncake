package com.mooncake.registry;

import com.mooncake.MooncakeMod;
import com.mooncake.item.HardenedArmorItem;
import com.mooncake.item.HardenedGearItems;
import com.mooncake.item.HardenedMooncakeTier;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.level.block.WeatheringCopper;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Hardened mooncake tools / weapons / armor / ranged — 8 weather×wax variants each.
 */
public final class ModHardenedGear {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MooncakeMod.MOD_ID);
    public static final List<DeferredItem<? extends Item>> ALL = new ArrayList<>();

    /** Default (unaffected) arrow — used as projectile pickup fallback. */
    public static DeferredItem<HardenedGearItems.Arrow> UNAFFECTED_ARROW;

    private static final Stage[] STAGES = {
            new Stage("", WeatheringCopper.WeatherState.UNAFFECTED, false),
            new Stage("exposed_", WeatheringCopper.WeatherState.EXPOSED, false),
            new Stage("weathered_", WeatheringCopper.WeatherState.WEATHERED, false),
            new Stage("oxidized_", WeatheringCopper.WeatherState.OXIDIZED, false),
            new Stage("waxed_", WeatheringCopper.WeatherState.UNAFFECTED, true),
            new Stage("waxed_exposed_", WeatheringCopper.WeatherState.EXPOSED, true),
            new Stage("waxed_weathered_", WeatheringCopper.WeatherState.WEATHERED, true),
            new Stage("waxed_oxidized_", WeatheringCopper.WeatherState.OXIDIZED, true)
    };

    static {
        for (Stage stage : STAGES) {
            registerSet(stage);
        }
    }

    private ModHardenedGear() {
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    private static void registerSet(Stage stage) {
        String p = stage.prefix;
        WeatheringCopper.WeatherState s = stage.state;
        boolean w = stage.waxed;

        ALL.add(ITEMS.register(p + "hardened_mooncake_sword", () -> new HardenedGearItems.Sword(
                s, w, new Item.Properties().attributes(SwordItem.createAttributes(HardenedMooncakeTier.INSTANCE, 3, -2.4F)))));
        ALL.add(ITEMS.register(p + "hardened_mooncake_pickaxe", () -> new HardenedGearItems.Pickaxe(
                s, w, new Item.Properties().attributes(DiggerItem.createAttributes(HardenedMooncakeTier.INSTANCE, 1.0F, -2.8F)))));
        ALL.add(ITEMS.register(p + "hardened_mooncake_axe", () -> new HardenedGearItems.Axe(
                s, w, new Item.Properties().attributes(DiggerItem.createAttributes(HardenedMooncakeTier.INSTANCE, 6.0F, -3.1F)))));
        ALL.add(ITEMS.register(p + "hardened_mooncake_shovel", () -> new HardenedGearItems.Shovel(
                s, w, new Item.Properties().attributes(DiggerItem.createAttributes(HardenedMooncakeTier.INSTANCE, 1.5F, -3.0F)))));
        ALL.add(ITEMS.register(p + "hardened_mooncake_hoe", () -> new HardenedGearItems.Hoe(
                s, w, new Item.Properties().attributes(DiggerItem.createAttributes(HardenedMooncakeTier.INSTANCE, -2.0F, -1.0F)))));

        ALL.add(ITEMS.register(p + "hardened_mooncake_helmet", () -> new HardenedArmorItem(
                s, w, ArmorItem.Type.HELMET,
                new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(18)))));
        ALL.add(ITEMS.register(p + "hardened_mooncake_chestplate", () -> new HardenedArmorItem(
                s, w, ArmorItem.Type.CHESTPLATE,
                new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(18)))));
        ALL.add(ITEMS.register(p + "hardened_mooncake_leggings", () -> new HardenedArmorItem(
                s, w, ArmorItem.Type.LEGGINGS,
                new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(18)))));
        ALL.add(ITEMS.register(p + "hardened_mooncake_boots", () -> new HardenedArmorItem(
                s, w, ArmorItem.Type.BOOTS,
                new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(18)))));

        ALL.add(ITEMS.register(p + "hardened_mooncake_shield", () -> new HardenedGearItems.Shield(
                s, w, new Item.Properties().durability(480))));
        ALL.add(ITEMS.register(p + "hardened_mooncake_trident", () -> new HardenedGearItems.Trident(
                s, w, new Item.Properties()
                        .rarity(Rarity.RARE)
                        .durability(400)
                        .attributes(TridentItem.createAttributes()))));
        ALL.add(ITEMS.register(p + "hardened_mooncake_mace", () -> new HardenedGearItems.Mace(
                s, w, new Item.Properties()
                        .rarity(Rarity.EPIC)
                        .durability(500)
                        .attributes(net.minecraft.world.item.MaceItem.createAttributes()))));
        ALL.add(ITEMS.register(p + "hardened_mooncake_bow", () -> new HardenedGearItems.Bow(
                s, w, new Item.Properties().durability(480))));
        ALL.add(ITEMS.register(p + "hardened_mooncake_crossbow", () -> new HardenedGearItems.Crossbow(
                s, w, new Item.Properties().durability(560))));

        DeferredItem<HardenedGearItems.Arrow> arrow = ITEMS.register(
                p + "hardened_mooncake_arrow",
                () -> new HardenedGearItems.Arrow(s, w, new Item.Properties()));
        ALL.add(arrow);
        if (stage.prefix.isEmpty()) {
            UNAFFECTED_ARROW = arrow;
        }
    }

    private record Stage(String prefix, WeatheringCopper.WeatherState state, boolean waxed) {
    }
}
