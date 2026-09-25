package com.mooncake.util;

import com.mooncake.component.MooncakeFillings;
import com.mooncake.config.MooncakeConfig;
import com.mooncake.data.EatEffectsManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;

/** Shared “eat this mooncake” application for food use and thrown hits. */
public final class MooncakeEating {
    private MooncakeEating() {
    }

    /**
     * Apply crust food (if any), filling/crust consumables, and datapack eat-effects.
     */
    public static void applyAsEaten(ItemStack mooncake, Level level, LivingEntity eater) {
        if (level.isClientSide || eater == null || mooncake.isEmpty()) {
            return;
        }

        FoodProperties crust = mooncake.getFoodProperties(eater);
        if (crust != null) {
            eater.eat(level, mooncake.copyWithCount(1), crust);
        }

        MooncakeFillings effectFillings = MooncakeFillings.forEffects(mooncake);
        applyIngredientConsumables(effectFillings, level, eater, MooncakeConfig.applyFillingFoodsOnEat());
        EatEffectsManager.INSTANCE.applyFromFillings(effectFillings, eater);
    }

    /**
     * Fully replay food fillings via {@code finishUsingItem} (nutrition + food effects).
     * Always also applies data-bearing non-foods (potions, milk, ominous bottle).
     */
    public static void applyFillingFoods(MooncakeFillings fillings, Level level, LivingEntity livingEntity) {
        applyIngredientConsumables(fillings, level, livingEntity, true);
    }

    /**
     * @param includeFoods when false, skip items that only have {@link FoodProperties};
     *                     potions / milk / ominous bottle still apply.
     */
    public static void applyIngredientConsumables(
            MooncakeFillings fillings,
            Level level,
            LivingEntity livingEntity,
            boolean includeFoods
    ) {
        if (fillings == null || fillings.isEmpty()) {
            return;
        }
        for (ItemStack filling : fillings.items()) {
            int times = Math.max(1, filling.getCount());
            for (int n = 0; n < times; n++) {
                ItemStack one = filling.copyWithCount(1);
                FoodProperties food = one.getFoodProperties(livingEntity);
                if (food != null) {
                    if (!includeFoods) {
                        continue;
                    }
                    giveLeftover(livingEntity, one.getItem().finishUsingItem(one, level, livingEntity));
                    continue;
                }
                applyDataBearingConsumable(one, level, livingEntity);
            }
        }
    }

    /**
     * Items whose effects live in data components rather than food (potions, milk, ominous bottle).
     */
    private static void applyDataBearingConsumable(ItemStack one, Level level, LivingEntity living) {
        PotionContents contents = one.get(DataComponents.POTION_CONTENTS);
        if (contents != null && contents.hasEffects()) {
            // Apply directly so splash/lingering still grant effects when baked in,
            // without dumping a glass bottle for every potion ingredient.
            applyPotionContents(contents, living);
            return;
        }
        if (one.is(Items.MILK_BUCKET) || one.has(DataComponents.OMINOUS_BOTTLE_AMPLIFIER)) {
            giveLeftover(living, one.getItem().finishUsingItem(one, level, living));
        }
    }

    private static void applyPotionContents(PotionContents contents, LivingEntity living) {
        Player player = living instanceof Player p ? p : null;
        contents.forEachEffect(effect -> {
            if (effect.getEffect().value().isInstantenous()) {
                effect.getEffect().value().applyInstantenousEffect(player, player, living, effect.getAmplifier(), 1.0);
            } else {
                living.addEffect(new MobEffectInstance(effect));
            }
        });
    }

    private static void giveLeftover(LivingEntity livingEntity, ItemStack leftover) {
        if (!(livingEntity instanceof Player player) || player.hasInfiniteMaterials()) {
            return;
        }
        if (leftover.isEmpty()) {
            return;
        }
        if (!player.getInventory().add(leftover)) {
            player.drop(leftover, false);
        }
    }
}
