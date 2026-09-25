package com.mooncake.item;

import com.mooncake.component.MooncakeFillings;
import com.mooncake.component.MooncakeWeather;
import com.mooncake.registry.ModArmorMaterials;
import com.mooncake.util.MooncakeNames;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.WeatheringCopper;

/**
 * Hardened mooncake armor. Fillings drive eat-style aura while worn —
 * wearers are declared via {@link com.mooncake.HardenedArmorAuraHandler} on equip / spawn
 * (players and mobs), not by scanning every living entity each tick.
 */
public class HardenedArmorItem extends ArmorItem implements HardenedWeatherItem {
    public enum Product {
        HELMET(
                "item.mooncake.filled_hardened_helmet",
                "item.mooncake.filled_hardened_helmet_truncated",
                "item.mooncake.gear.helmet"),
        CHESTPLATE(
                "item.mooncake.filled_hardened_chestplate",
                "item.mooncake.filled_hardened_chestplate_truncated",
                "item.mooncake.gear.chestplate"),
        LEGGINGS(
                "item.mooncake.filled_hardened_leggings",
                "item.mooncake.filled_hardened_leggings_truncated",
                "item.mooncake.gear.leggings"),
        BOOTS(
                "item.mooncake.filled_hardened_boots",
                "item.mooncake.filled_hardened_boots_truncated",
                "item.mooncake.gear.boots");

        public final String filledKey;
        public final String truncatedKey;
        public final String emptyKey;

        Product(String filledKey, String truncatedKey, String emptyKey) {
            this.filledKey = filledKey;
            this.truncatedKey = truncatedKey;
            this.emptyKey = emptyKey;
        }

        public static Product of(ArmorItem.Type type) {
            return switch (type) {
                case HELMET -> HELMET;
                case CHESTPLATE -> CHESTPLATE;
                case LEGGINGS -> LEGGINGS;
                case BOOTS -> BOOTS;
                case BODY -> CHESTPLATE;
            };
        }
    }

    private final WeatheringCopper.WeatherState state;
    private final boolean waxed;
    private final Product product;

    public HardenedArmorItem(
            WeatheringCopper.WeatherState state,
            boolean waxed,
            ArmorItem.Type type,
            Properties properties
    ) {
        super(ModArmorMaterials.forState(state), type, properties);
        this.state = state;
        this.waxed = waxed;
        this.product = Product.of(type);
    }

    @Override
    public MooncakeWeather weather() {
        return new MooncakeWeather(state, waxed);
    }

    @Override
    public Component getName(ItemStack stack) {
        MooncakeFillings.stripRedundantWeather(stack);
        return MooncakeNames.forStackProduct(
                stack,
                new MooncakeWeather(state, waxed),
                product.filledKey,
                product.truncatedKey,
                product.emptyKey,
                true
        );
    }
}
