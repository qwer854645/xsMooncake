package com.mooncake.util;

import com.mooncake.component.MooncakeCrust;
import com.mooncake.component.MooncakeFillings;
import com.mooncake.data.CrustRule;
import com.mooncake.data.CrustRulesManager;
import com.mooncake.data.FillingRule;
import com.mooncake.data.FillingRulesManager;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

/** Shared nutrition / saturation bonuses from fillings and crust extras. */
public final class MooncakeNutrition {
    private MooncakeNutrition() {
    }

    public record Bonus(int nutrition, float saturation) {
        public static final Bonus ZERO = new Bonus(0, 0.0F);

        public Bonus add(Bonus other) {
            return new Bonus(nutrition + other.nutrition, saturation + other.saturation);
        }

        public Bonus scale(int numerator, int denominator) {
            if (denominator <= 0) {
                return ZERO;
            }
            return new Bonus(
                    Math.max(0, nutrition * numerator / denominator),
                    saturation * numerator / (float) denominator
            );
        }
    }

    public static Bonus fromStack(ItemStack mooncake) {
        return fromFillings(MooncakeFillings.get(mooncake).items())
                .add(fromCrust(MooncakeCrust.get(mooncake).items()));
    }

    public static Bonus fromFillings(List<ItemStack> items) {
        return sum(items, true);
    }

    public static Bonus fromCrust(List<ItemStack> items) {
        return sum(items, false);
    }

    private static Bonus sum(List<ItemStack> items, boolean filling) {
        int nutrition = 0;
        float saturation = 0.0F;
        for (ItemStack stack : items) {
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            int count = Math.max(1, stack.getCount());
            Integer ruleNutrition = null;
            Float ruleSaturation = null;
            if (filling) {
                FillingRule rule = FillingRulesManager.INSTANCE.get(id);
                if (rule != null) {
                    ruleNutrition = rule.nutritionBonus();
                    ruleSaturation = rule.saturationBonus();
                }
            } else {
                CrustRule rule = CrustRulesManager.INSTANCE.get(id);
                if (rule != null) {
                    ruleNutrition = rule.nutritionBonus();
                    ruleSaturation = rule.saturationBonus();
                }
            }
            if (ruleNutrition != null) {
                nutrition += ruleNutrition * count;
                saturation += ruleSaturation * count;
            } else {
                FoodProperties food = stack.get(DataComponents.FOOD);
                if (food != null) {
                    nutrition += Math.max(1, food.nutrition() / 2) * count;
                    saturation += (food.saturation() / 2.0F) * count;
                } else {
                    nutrition += count;
                    saturation += 0.1F * count;
                }
            }
        }
        return new Bonus(nutrition, saturation);
    }
}
