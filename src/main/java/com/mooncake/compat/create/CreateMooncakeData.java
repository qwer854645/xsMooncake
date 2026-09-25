package com.mooncake.compat.create;

import com.mooncake.MooncakeMod;
import com.mooncake.component.MooncakeCrust;
import com.mooncake.component.MooncakeFillings;
import com.mooncake.component.MooncakeWeather;
import com.mooncake.item.MooncakeBlockItem;
import com.mooncake.registry.ModComponents;
import com.mooncake.util.MooncakeDataTransfer;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/** Copies mooncake data components across Create processing outputs. */
public final class CreateMooncakeData {
    private CreateMooncakeData() {
    }

    public static boolean isMooncakeItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return MooncakeMod.MOD_ID.equals(id.getNamespace());
    }

    public static boolean hasMooncakeData(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return stack.has(ModComponents.FILLINGS.get())
                || stack.has(ModComponents.CRUST.get())
                || stack.has(ModComponents.WEATHER.get())
                || !MooncakeFillings.get(stack).isEmpty()
                || !MooncakeCrust.get(stack).isEmpty();
    }

    /** True when Create processing must preserve or produce mooncake item data. */
    public static boolean involvesMooncake(ItemStack stack) {
        return isMooncakeItem(stack) || hasMooncakeData(stack);
    }

    /**
     * Only rewrite Create cooking application when input or result is mooncake-related.
     * Leaves every other mod's fan / belt cooking on Create's default path.
     */
    public static boolean shouldRewriteCooking(ItemStack input, ItemStack recipeResult) {
        return involvesMooncake(input) || isMooncakeItem(recipeResult);
    }

    /**
     * Copy fillings + crust from {@code from} onto {@code to} when the target is missing them.
     * Never overwrites non-empty data already present on the output.
     * Weather is not transferred — cakes/gear encode oxidation in the item id.
     */
    public static void transfer(ItemStack from, ItemStack to) {
        if (from == null || to == null || from.isEmpty() || to.isEmpty()) {
            return;
        }
        if (!involvesMooncake(from) && !isMooncakeItem(to)) {
            return;
        }
        MooncakeDataTransfer.copyMissing(from, to);
    }

    public static MooncakeWeather resolveWeather(ItemStack stack) {
        MooncakeWeather stored = MooncakeWeather.get(stack);
        if (!stored.equals(MooncakeWeather.DEFAULT)) {
            return stored;
        }
        if (stack.getItem() instanceof MooncakeBlockItem blockItem) {
            return MooncakeWeather.fromBlock(blockItem.getBlock());
        }
        if (stack.getItem() instanceof BlockItem blockItem) {
            return MooncakeWeather.fromBlock(blockItem.getBlock());
        }
        return stored;
    }

    public static void transferToAll(ItemStack from, List<ItemStack> outputs) {
        if (outputs == null || outputs.isEmpty()) {
            return;
        }
        for (ItemStack out : outputs) {
            transfer(from, out);
        }
    }

    /** Merge fillings + crust from several consumed inputs onto a single result (e.g. basin crafting). */
    public static void mergeFillingsFrom(List<ItemStack> sources, ItemStack result) {
        MooncakeDataTransfer.mergeFromSources(sources, result);
    }
}
