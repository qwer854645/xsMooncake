package com.mooncake.util;

import com.mooncake.block.MooncakeBlocks;
import com.mooncake.component.MooncakeCrust;
import com.mooncake.component.MooncakeFillings;
import com.mooncake.component.MooncakeWeather;
import com.mooncake.item.HardenedBuildingBlockItem;
import com.mooncake.registry.ModBlocks;
import com.mooncake.registry.ModHardenedBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

/** Shared display names for inventory, Jade, and other UIs. */
public final class MooncakeNames {
    private MooncakeNames() {
    }

    public static Component forBlock(BlockState state, MooncakeFillings fillings) {
        return forBlock(state.getBlock(), fillings, MooncakeCrust.EMPTY);
    }

    public static Component forBlock(BlockState state, MooncakeFillings fillings, MooncakeCrust crust) {
        return forBlock(state.getBlock(), fillings, crust);
    }

    public static Component forBlock(Block block, MooncakeFillings fillings) {
        return forBlock(block, fillings, MooncakeCrust.EMPTY);
    }

    public static Component forBlock(Block block, MooncakeFillings fillings, MooncakeCrust crust) {
        if (ModHardenedBlocks.isFamily(block)) {
            return forHardenedBuilding(block, fillings, crust);
        }
        if (MooncakeBlocks.isPiece(block)) {
            return forPiece(block, fillings, crust);
        }
        MooncakeWeather weather = ModBlocks.weatherOf(block);
        boolean hardened = MooncakeBlocks.isHardenedCake(block);
        if ((fillings == null || fillings.isEmpty()) && (crust == null || crust.isEmpty())) {
            // Block translation already includes spoilage / wrap stage.
            return block.getName();
        }
        return finishName(
                fillings == null ? MooncakeFillings.EMPTY : fillings,
                crust == null ? MooncakeCrust.EMPTY : crust,
                weather,
                hardened ? "item.mooncake.filled_hardened" : "item.mooncake.filled",
                hardened ? "item.mooncake.filled_hardened_truncated" : "item.mooncake.filled_truncated",
                hardened ? "item.mooncake.hardened_mooncake" : "item.mooncake.mooncake",
                false
        );
    }

    public static Component forHardenedBuilding(Block block, MooncakeFillings fillings) {
        return forHardenedBuilding(block, fillings, MooncakeCrust.EMPTY);
    }

    public static Component forHardenedBuilding(Block block, MooncakeFillings fillings, MooncakeCrust crust) {
        MooncakeWeather weather = ModHardenedBlocks.weatherOf(block);
        HardenedBuildingBlockItem.Product product = productOf(block);
        if (product == null) {
            return block.getName();
        }
        if ((fillings == null || fillings.isEmpty()) && (crust == null || crust.isEmpty())) {
            // Block translation already includes spoilage / wrap stage.
            return block.getName();
        }
        return finishName(
                fillings == null ? MooncakeFillings.EMPTY : fillings,
                crust == null ? MooncakeCrust.EMPTY : crust,
                weather,
                product.filledKey,
                product.truncatedKey,
                product.emptyKey,
                true
        );
    }

    private static HardenedBuildingBlockItem.Product productOf(Block block) {
        Item item = block.asItem();
        if (item instanceof HardenedBuildingBlockItem building) {
            return building.product();
        }
        if (item instanceof HardenedBuildingBlockItem.Door) {
            return HardenedBuildingBlockItem.Product.DOOR;
        }
        return null;
    }

    public static Component forPiece(Block block, MooncakeFillings fillings) {
        return forPiece(block, fillings, MooncakeCrust.EMPTY);
    }

    public static Component forPiece(Block block, MooncakeFillings fillings, MooncakeCrust crust) {
        MooncakeWeather weather = ModBlocks.weatherOf(block);
        boolean hardened = MooncakeBlocks.isHardenedCake(block);
        String emptyKey = hardened ? "item.mooncake.hardened_mooncake_piece" : "item.mooncake.mooncake_piece";
        if ((fillings == null || fillings.isEmpty()) && (crust == null || crust.isEmpty())) {
            return withWeather(Component.translatable(emptyKey), weather);
        }
        return finishName(
                fillings == null ? MooncakeFillings.EMPTY : fillings,
                crust == null ? MooncakeCrust.EMPTY : crust,
                weather,
                hardened ? "item.mooncake.filled_hardened_piece" : "item.mooncake.filled_piece",
                hardened ? "item.mooncake.filled_hardened_piece_truncated" : "item.mooncake.filled_piece_truncated",
                emptyKey,
                hardened
        );
    }

    /** Shared naming: fillings base → optional {@code xx皮 } wrap → weather prefix. */
    public static Component finishName(
            MooncakeFillings fillings,
            MooncakeCrust crust,
            MooncakeWeather weather,
            String filledKey,
            String truncatedKey,
            String emptyKey,
            boolean mooncakeUnits
    ) {
        Component base = fillings.displayProductBase(
                MooncakeFillings.isShiftDownClient(),
                filledKey,
                truncatedKey,
                emptyKey,
                mooncakeUnits
        );
        if (crust != null && !crust.isEmpty()) {
            base = crust.applyToName(base);
        }
        return withWeather(base, weather);
    }

    public static Component forStackProduct(
            ItemStack stack,
            MooncakeWeather weather,
            String filledKey,
            String truncatedKey,
            String emptyKey,
            boolean mooncakeUnits
    ) {
        return finishName(
                MooncakeFillings.get(stack),
                MooncakeCrust.get(stack),
                weather,
                filledKey,
                truncatedKey,
                emptyKey,
                mooncakeUnits
        );
    }

    /** Apply spoilage / wrap prefixes to a base name. */
    public static Component withWeather(Component base, MooncakeWeather weather) {
        if (weather.state() == WeatheringCopper.WeatherState.UNAFFECTED && !weather.waxed()) {
            return base;
        }
        String key = switch (weather.state()) {
            case UNAFFECTED -> "item.mooncake.weather.waxed";
            case EXPOSED -> weather.waxed()
                    ? "item.mooncake.weather.waxed_exposed"
                    : "item.mooncake.weather.exposed";
            case WEATHERED -> weather.waxed()
                    ? "item.mooncake.weather.waxed_weathered"
                    : "item.mooncake.weather.weathered";
            case OXIDIZED -> weather.waxed()
                    ? "item.mooncake.weather.waxed_oxidized"
                    : "item.mooncake.weather.oxidized";
        };
        return Component.translatable(key, base);
    }
}
