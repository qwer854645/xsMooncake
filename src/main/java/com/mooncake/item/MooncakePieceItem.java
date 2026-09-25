package com.mooncake.item;

import com.mooncake.component.MooncakeCrust;
import com.mooncake.component.MooncakeFillings;
import com.mooncake.config.MooncakeConfig;
import com.mooncake.util.MooncakeNames;
import com.mooncake.util.MooncakeNutrition;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** Soft mooncake quarter — eat (fast) or place only. */
public class MooncakePieceItem extends MooncakeBlockItem {
    private static final int BASE_NUTRITION = 1;
    private static final float BASE_SATURATION = 0.1F;
    private static final int MAX_NUTRITION = 5;
    private static final float MAX_SATURATION = 0.4F;

    public MooncakePieceItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        MooncakeFillings.stripRedundantWeather(stack);
        return MooncakeNames.forPiece(
                this.getBlock(),
                MooncakeFillings.get(stack),
                MooncakeCrust.get(stack)
        );
    }

    @Nullable
    @Override
    public FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        int baseNutrition = BASE_NUTRITION;
        float baseSaturation = BASE_SATURATION;

        if (MooncakeConfig.applyFillingFoodsOnEat()) {
            return new FoodProperties.Builder()
                    .nutrition(baseNutrition)
                    .saturationModifier(baseSaturation)
                    .alwaysEdible()
                    .fast()
                    .build();
        }

        // Quarter of whole-cake filling/crust bonus.
        MooncakeNutrition.Bonus bonus = MooncakeNutrition.fromStack(stack).scale(1, 4);
        int nutrition = Math.min(MAX_NUTRITION, Math.max(1, baseNutrition + bonus.nutrition()));
        float saturation = Math.min(MAX_SATURATION, baseSaturation + bonus.saturation());

        return new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationModifier(saturation)
                .alwaysEdible()
                .fast()
                .build();
    }
}
