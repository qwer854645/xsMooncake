package com.mooncake.util;

import com.mooncake.component.MooncakeWeather;
import com.mooncake.item.HardenedBuildingBlockItem;
import com.mooncake.item.HardenedWeatherItem;
import com.mooncake.item.MooncakeBlockItem;
import com.mooncake.registry.ModBlocks;
import com.mooncake.registry.ModHardenedBlocks;
import com.mooncake.registry.ModTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.level.block.Block;

/** Crafting rules: mooncake ingredients must share oxidation + wax. */
public final class MooncakeCraftWeather {
    private MooncakeCraftWeather() {
    }

    public static boolean isWeatherBearing(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        if (item instanceof MooncakeBlockItem
                || item instanceof HardenedBuildingBlockItem
                || item instanceof HardenedBuildingBlockItem.Door
                || item instanceof HardenedWeatherItem) {
            return true;
        }
        return stack.is(ModTags.HARDENED_MOONCAKE_CAKES);
    }

    public static MooncakeWeather fromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return MooncakeWeather.DEFAULT;
        }
        Item item = stack.getItem();
        if (item instanceof HardenedWeatherItem weatherItem) {
            return weatherItem.weather();
        }
        if (item instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            return ModBlocks.weatherOf(block);
        }
        return MooncakeWeather.get(stack);
    }

    /**
     * Every weather-bearing slot must share the same oxidation + wax.
     * Non-mooncake ingredients (sticks, etc.) are ignored.
     */
    public static boolean allSame(CraftingInput input) {
        MooncakeWeather expected = null;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!isWeatherBearing(stack)) {
                continue;
            }
            MooncakeWeather weather = fromStack(stack);
            if (expected == null) {
                expected = weather;
            } else if (!expected.equals(weather)) {
                return false;
            }
        }
        return true;
    }

    /** First weather-bearing stack's state, or {@link MooncakeWeather#DEFAULT}. */
    public static MooncakeWeather first(CraftingInput input) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (isWeatherBearing(stack)) {
                return fromStack(stack);
            }
        }
        return MooncakeWeather.DEFAULT;
    }

    public static Block hardenedBlockFor(MooncakeWeather weather) {
        return ModHardenedBlocks.blockFor(weather);
    }
}
