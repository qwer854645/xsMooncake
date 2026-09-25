package com.mooncake.util;

import com.mooncake.data.CrustRule;
import com.mooncake.data.CrustRulesManager;
import com.mooncake.registry.ModTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class CrustHelper {
    private CrustHelper() {
    }

    /**
     * Crust extras are blacklist-default (same as fillings):
     * banned never; optional whitelist if {@code #valid_crusts} is non-empty; otherwise any item.
     */
    public static boolean isValidCrust(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (MooncakeSeriesGuard.isBlockedAsIngredient(stack)) {
            return false;
        }
        if (stack.is(ModTags.BANNED_CRUSTS)) {
            return false;
        }
        var whitelist = BuiltInRegistries.ITEM.getTag(ModTags.VALID_CRUSTS);
        if (whitelist.isPresent() && whitelist.get().size() > 0 && !stack.is(ModTags.VALID_CRUSTS)) {
            return false;
        }
        return true;
    }

    public static Component crustName(ItemStack stack) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        CrustRule rule = CrustRulesManager.INSTANCE.get(id);
        if (rule != null && rule.nameKey().isPresent()) {
            return Component.translatable(rule.nameKey().get());
        }
        return FillingHelper.fillingName(stack);
    }

    public static Component crustLabel(ItemStack stack) {
        Component name = crustName(stack);
        if (stack.getCount() > 1) {
            return Component.translatable("item.mooncake.filling_counted", name, stack.getCount());
        }
        return name;
    }
}
