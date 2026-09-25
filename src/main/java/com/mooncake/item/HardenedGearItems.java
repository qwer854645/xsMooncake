package com.mooncake.item;

import com.mooncake.component.MooncakeFillings;
import com.mooncake.component.MooncakeWeather;
import com.mooncake.entity.HardenedMooncakeArrow;
import com.mooncake.util.MooncakeNames;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WeatheringCopper;

public final class HardenedGearItems {
    public enum Product {
        SWORD(
                "item.mooncake.filled_hardened_sword",
                "item.mooncake.filled_hardened_sword_truncated",
                "item.mooncake.gear.sword"),
        PICKAXE(
                "item.mooncake.filled_hardened_pickaxe",
                "item.mooncake.filled_hardened_pickaxe_truncated",
                "item.mooncake.gear.pickaxe"),
        AXE(
                "item.mooncake.filled_hardened_axe",
                "item.mooncake.filled_hardened_axe_truncated",
                "item.mooncake.gear.axe"),
        SHOVEL(
                "item.mooncake.filled_hardened_shovel",
                "item.mooncake.filled_hardened_shovel_truncated",
                "item.mooncake.gear.shovel"),
        HOE(
                "item.mooncake.filled_hardened_hoe",
                "item.mooncake.filled_hardened_hoe_truncated",
                "item.mooncake.gear.hoe"),
        SHIELD(
                "item.mooncake.filled_hardened_shield",
                "item.mooncake.filled_hardened_shield_truncated",
                "item.mooncake.gear.shield"),
        TRIDENT(
                "item.mooncake.filled_hardened_trident",
                "item.mooncake.filled_hardened_trident_truncated",
                "item.mooncake.gear.trident"),
        MACE(
                "item.mooncake.filled_hardened_mace",
                "item.mooncake.filled_hardened_mace_truncated",
                "item.mooncake.gear.mace"),
        BOW(
                "item.mooncake.filled_hardened_bow",
                "item.mooncake.filled_hardened_bow_truncated",
                "item.mooncake.gear.bow"),
        CROSSBOW(
                "item.mooncake.filled_hardened_crossbow",
                "item.mooncake.filled_hardened_crossbow_truncated",
                "item.mooncake.gear.crossbow"),
        ARROW(
                "item.mooncake.filled_hardened_arrow",
                "item.mooncake.filled_hardened_arrow_truncated",
                "item.mooncake.gear.arrow");

        public final String filledKey;
        public final String truncatedKey;
        public final String emptyKey;

        Product(String filledKey, String truncatedKey, String emptyKey) {
            this.filledKey = filledKey;
            this.truncatedKey = truncatedKey;
            this.emptyKey = emptyKey;
        }
    }

    private HardenedGearItems() {
    }

    static Component productName(
            ItemStack stack, WeatheringCopper.WeatherState state, boolean waxed, Product product
    ) {
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

    public static class Sword extends SwordItem implements HardenedWeatherItem {
        private final WeatheringCopper.WeatherState state;
        private final boolean waxed;

        public Sword(WeatheringCopper.WeatherState state, boolean waxed, Properties properties) {
            super(HardenedMooncakeTier.INSTANCE, properties);
            this.state = state;
            this.waxed = waxed;
        }

        @Override
        public MooncakeWeather weather() {
            return new MooncakeWeather(state, waxed);
        }

        @Override
        public Component getName(ItemStack stack) {
            return productName(stack, state, waxed, Product.SWORD);
        }
    }

    public static class Pickaxe extends PickaxeItem implements HardenedWeatherItem {
        private final WeatheringCopper.WeatherState state;
        private final boolean waxed;

        public Pickaxe(WeatheringCopper.WeatherState state, boolean waxed, Properties properties) {
            super(HardenedMooncakeTier.INSTANCE, properties);
            this.state = state;
            this.waxed = waxed;
        }

        @Override
        public MooncakeWeather weather() {
            return new MooncakeWeather(state, waxed);
        }

        @Override
        public Component getName(ItemStack stack) {
            return productName(stack, state, waxed, Product.PICKAXE);
        }
    }

    public static class Axe extends AxeItem implements HardenedWeatherItem {
        private final WeatheringCopper.WeatherState state;
        private final boolean waxed;

        public Axe(WeatheringCopper.WeatherState state, boolean waxed, Properties properties) {
            super(HardenedMooncakeTier.INSTANCE, properties);
            this.state = state;
            this.waxed = waxed;
        }

        @Override
        public MooncakeWeather weather() {
            return new MooncakeWeather(state, waxed);
        }

        @Override
        public Component getName(ItemStack stack) {
            return productName(stack, state, waxed, Product.AXE);
        }
    }

    public static class Shovel extends ShovelItem implements HardenedWeatherItem {
        private final WeatheringCopper.WeatherState state;
        private final boolean waxed;

        public Shovel(WeatheringCopper.WeatherState state, boolean waxed, Properties properties) {
            super(HardenedMooncakeTier.INSTANCE, properties);
            this.state = state;
            this.waxed = waxed;
        }

        @Override
        public MooncakeWeather weather() {
            return new MooncakeWeather(state, waxed);
        }

        @Override
        public Component getName(ItemStack stack) {
            return productName(stack, state, waxed, Product.SHOVEL);
        }
    }

    public static class Hoe extends HoeItem implements HardenedWeatherItem {
        private final WeatheringCopper.WeatherState state;
        private final boolean waxed;

        public Hoe(WeatheringCopper.WeatherState state, boolean waxed, Properties properties) {
            super(HardenedMooncakeTier.INSTANCE, properties);
            this.state = state;
            this.waxed = waxed;
        }

        @Override
        public MooncakeWeather weather() {
            return new MooncakeWeather(state, waxed);
        }

        @Override
        public Component getName(ItemStack stack) {
            return productName(stack, state, waxed, Product.HOE);
        }
    }

    public static class Shield extends ShieldItem implements HardenedWeatherItem {
        private final WeatheringCopper.WeatherState state;
        private final boolean waxed;

        public Shield(WeatheringCopper.WeatherState state, boolean waxed, Properties properties) {
            super(properties);
            this.state = state;
            this.waxed = waxed;
        }

        @Override
        public MooncakeWeather weather() {
            return new MooncakeWeather(state, waxed);
        }

        @Override
        public Component getName(ItemStack stack) {
            return productName(stack, state, waxed, Product.SHIELD);
        }
    }

    public static class Trident extends TridentItem implements HardenedWeatherItem {
        private final WeatheringCopper.WeatherState state;
        private final boolean waxed;

        public Trident(WeatheringCopper.WeatherState state, boolean waxed, Properties properties) {
            super(properties);
            this.state = state;
            this.waxed = waxed;
        }

        @Override
        public MooncakeWeather weather() {
            return new MooncakeWeather(state, waxed);
        }

        @Override
        public Component getName(ItemStack stack) {
            return productName(stack, state, waxed, Product.TRIDENT);
        }
    }

    public static class Mace extends MaceItem implements HardenedWeatherItem {
        private final WeatheringCopper.WeatherState state;
        private final boolean waxed;

        public Mace(WeatheringCopper.WeatherState state, boolean waxed, Properties properties) {
            super(properties);
            this.state = state;
            this.waxed = waxed;
        }

        @Override
        public MooncakeWeather weather() {
            return new MooncakeWeather(state, waxed);
        }

        @Override
        public Component getName(ItemStack stack) {
            return productName(stack, state, waxed, Product.MACE);
        }
    }

    public static class Bow extends BowItem implements HardenedWeatherItem {
        private final WeatheringCopper.WeatherState state;
        private final boolean waxed;

        public Bow(WeatheringCopper.WeatherState state, boolean waxed, Properties properties) {
            super(properties);
            this.state = state;
            this.waxed = waxed;
        }

        @Override
        public MooncakeWeather weather() {
            return new MooncakeWeather(state, waxed);
        }

        @Override
        public Component getName(ItemStack stack) {
            return productName(stack, state, waxed, Product.BOW);
        }
    }

    public static class Crossbow extends CrossbowItem implements HardenedWeatherItem {
        private final WeatheringCopper.WeatherState state;
        private final boolean waxed;

        public Crossbow(WeatheringCopper.WeatherState state, boolean waxed, Properties properties) {
            super(properties);
            this.state = state;
            this.waxed = waxed;
        }

        @Override
        public MooncakeWeather weather() {
            return new MooncakeWeather(state, waxed);
        }

        @Override
        public Component getName(ItemStack stack) {
            return productName(stack, state, waxed, Product.CROSSBOW);
        }
    }

    public static class Arrow extends ArrowItem implements HardenedWeatherItem {
        private final WeatheringCopper.WeatherState state;
        private final boolean waxed;

        public Arrow(WeatheringCopper.WeatherState state, boolean waxed, Properties properties) {
            super(properties);
            this.state = state;
            this.waxed = waxed;
        }

        @Override
        public MooncakeWeather weather() {
            return new MooncakeWeather(state, waxed);
        }

        @Override
        public Component getName(ItemStack stack) {
            return productName(stack, state, waxed, Product.ARROW);
        }

        @Override
        public AbstractArrow createArrow(Level level, ItemStack ammo, LivingEntity shooter, @Nullable ItemStack weapon) {
            return new HardenedMooncakeArrow(level, shooter, ammo.copyWithCount(1), weapon);
        }
    }
}
