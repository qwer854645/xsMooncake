package com.mooncake.util;

import com.mooncake.data.FillingRule;
import com.mooncake.data.FillingRulesManager;
import com.mooncake.registry.ModTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class FillingHelper {
    private FillingHelper() {
    }

    public static boolean isValidFilling(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (MooncakeSeriesGuard.isBlockedAsIngredient(stack)) {
            return false;
        }
        if (stack.is(ModTags.BANNED_FILLINGS)) {
            return false;
        }
        // Whitelist is optional: empty tag means any non-banned item is allowed.
        var whitelist = BuiltInRegistries.ITEM.getTag(ModTags.VALID_FILLINGS);
        if (whitelist.isPresent() && whitelist.get().size() > 0 && !stack.is(ModTags.VALID_FILLINGS)) {
            return false;
        }
        return true;
    }

    public static Component fillingName(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        FillingRule rule = FillingRulesManager.INSTANCE.get(id);
        if (rule != null && rule.nameKey().isPresent()) {
            return Component.translatable(rule.nameKey().get());
        }
        return stack.getHoverName();
    }

    /** Name with {@code ×N} when count &gt; 1 (same item + components are already merged). */
    public static Component fillingLabel(ItemStack stack) {
        Component name = fillingName(stack);
        if (stack.getCount() > 1) {
            return Component.translatable("item.mooncake.filling_counted", name, stack.getCount());
        }
        return name;
    }
}
