package com.mooncake.util;

import com.mooncake.block.MooncakeBlocks;
import com.mooncake.component.MooncakeWeather;
import com.mooncake.item.HardenedWeatherItem;
import com.mooncake.registry.ModBlocks;
import com.mooncake.registry.ModHardenedBlocks;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;

/** Lookups for oxidation / wax item variants (path-based for gear, family-based for blocks). */
public final class MooncakeItemVariants {
    private MooncakeItemVariants() {
    }

    /** Next unwaxed oxidation stage for hardened gear / armor, or empty. */
    public static Optional<Item> nextOxidation(Item item) {
        if (!(item instanceof HardenedWeatherItem weatherItem) || weatherItem.weather().waxed()) {
            return Optional.empty();
        }
        WeatheringCopper.WeatherState state = weatherItem.weather().state();
        if (state == WeatheringCopper.WeatherState.OXIDIZED) {
            return Optional.empty();
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        String path = id.getPath();
        String nextPath = switch (state) {
            case UNAFFECTED -> "exposed_" + path;
            case EXPOSED -> path.replaceFirst("^exposed_", "weathered_");
            case WEATHERED -> path.replaceFirst("^weathered_", "oxidized_");
            case OXIDIZED -> path;
        };
        return lookup(id.getNamespace(), nextPath);
    }

    /** Waxed counterpart of an unwaxed mooncake-related item, or empty. */
    public static Optional<Item> waxedVersion(Item item) {
        if (item instanceof HardenedWeatherItem weatherItem) {
            if (weatherItem.weather().waxed()) {
                return Optional.empty();
            }
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
            String path = id.getPath();
            String waxedPath = switch (weatherItem.weather().state()) {
                case UNAFFECTED -> "waxed_" + path;
                case EXPOSED -> path.replaceFirst("^exposed_", "waxed_exposed_");
                case WEATHERED -> path.replaceFirst("^weathered_", "waxed_weathered_");
                case OXIDIZED -> path.replaceFirst("^oxidized_", "waxed_oxidized_");
            };
            return lookup(id.getNamespace(), waxedPath);
        }
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            return waxedBlock(block).map(Block::asItem);
        }
        return Optional.empty();
    }

    public static Optional<Block> waxedBlock(Block block) {
        if (MooncakeBlocks.isMooncake(block)) {
            MooncakeWeather weather = ModBlocks.weatherOf(block);
            if (weather.waxed()) {
                return Optional.empty();
            }
            boolean hardened = MooncakeBlocks.isHardenedCake(block);
            boolean piece = MooncakeBlocks.isPiece(block);
            return Optional.of(ModBlocks.byWeather(weather.state(), true, hardened, piece));
        }
        if (ModHardenedBlocks.isFamily(block)) {
            return ModHardenedBlocks.waxedCounterpart(block);
        }
        return Optional.empty();
    }

    /** Unwrapped counterpart of a wrapped mooncake-family block. */
    public static Optional<Block> unwaxedBlock(Block block) {
        if (MooncakeBlocks.isMooncake(block)) {
            MooncakeWeather weather = ModBlocks.weatherOf(block);
            if (!weather.waxed()) {
                return Optional.empty();
            }
            boolean hardened = MooncakeBlocks.isHardenedCake(block);
            boolean piece = MooncakeBlocks.isPiece(block);
            return Optional.of(ModBlocks.byWeather(weather.state(), false, hardened, piece));
        }
        if (ModHardenedBlocks.isFamily(block)) {
            return ModHardenedBlocks.unwaxedCounterpart(block);
        }
        return Optional.empty();
    }

    /** One spoilage stage milder (unwrapped only). */
    public static Optional<Block> previousOxidationBlock(Block block) {
        if (MooncakeBlocks.isMooncake(block)) {
            MooncakeWeather weather = ModBlocks.weatherOf(block);
            if (weather.waxed()) {
                return Optional.empty();
            }
            WeatheringCopper.WeatherState prev = switch (weather.state()) {
                case UNAFFECTED -> null;
                case EXPOSED -> WeatheringCopper.WeatherState.UNAFFECTED;
                case WEATHERED -> WeatheringCopper.WeatherState.EXPOSED;
                case OXIDIZED -> WeatheringCopper.WeatherState.WEATHERED;
            };
            if (prev == null) {
                return Optional.empty();
            }
            boolean hardened = MooncakeBlocks.isHardenedCake(block);
            boolean piece = MooncakeBlocks.isPiece(block);
            return Optional.of(ModBlocks.byWeather(prev, false, hardened, piece));
        }
        if (ModHardenedBlocks.isFamily(block)) {
            return ModHardenedBlocks.previousOxidation(block);
        }
        return Optional.empty();
    }

    /**
     * Axe scrape result: unwrap first, otherwise reduce spoilage by one stage.
     */
    public static Optional<Block> axeScrapeBlock(Block block) {
        Optional<Block> unwaxed = unwaxedBlock(block);
        if (unwaxed.isPresent()) {
            return unwaxed;
        }
        return previousOxidationBlock(block);
    }

    public static boolean canWax(ItemStack stack) {
        return stack != null && !stack.isEmpty() && waxedVersion(stack.getItem()).isPresent();
    }

    /** Replace item type while keeping count, damage, fillings, and other components. */
    public static ItemStack transmute(ItemStack stack, Item to) {
        return stack.transmuteCopy(to, stack.getCount());
    }

    private static Optional<Item> lookup(String namespace, String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace, path);
        if (!BuiltInRegistries.ITEM.containsKey(id)) {
            return Optional.empty();
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        return item == null ? Optional.empty() : Optional.of(item);
    }
}
