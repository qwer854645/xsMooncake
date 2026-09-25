package com.mooncake.util;

import com.mooncake.MooncakeMod;
import com.mooncake.config.MooncakeConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Blocks recursive mooncake-in-mooncake when configured. */
public final class MooncakeSeriesGuard {
    private MooncakeSeriesGuard() {
    }

    public static boolean isMooncakeSeriesItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(MooncakeMod.MOD_ID);
    }

    /** True when this stack must not be used as filling / crust extra. */
    public static boolean isBlockedAsIngredient(ItemStack stack) {
        return MooncakeConfig.banMooncakeSeriesAsIngredients() && isMooncakeSeriesItem(stack);
    }

    public static void warnPlayer(Player player) {
        if (player == null || player.level().isClientSide) {
            return;
        }
        player.displayClientMessage(Component.translatable("message.mooncake.no_mooncake_series"), true);
    }
}
