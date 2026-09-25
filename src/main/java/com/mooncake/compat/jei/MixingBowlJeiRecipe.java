package com.mooncake.compat.jei;

import com.mooncake.blockentity.MixingBowlBlockEntity;
import com.mooncake.registry.ModItems;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/** Synthetic JEI recipes for the mixing bowl. */
public record MixingBowlJeiRecipe(List<Ingredient> inputs, ItemStack output) {
    /** wheat×2 + egg + sugar → incomplete dough×2 */
    public static MixingBowlJeiRecipe incomplete() {
        return new MixingBowlJeiRecipe(
                List.of(
                        Ingredient.of(Items.WHEAT),
                        Ingredient.of(Items.WHEAT),
                        Ingredient.of(Items.EGG),
                        Ingredient.of(Items.SUGAR)
                ),
                new ItemStack(ModItems.INCOMPLETE_MOONCAKE_DOUGH.get(), MixingBowlBlockEntity.OUTPUT_COUNT)
        );
    }

    /** incomplete dough (+ optional extras in-world) → finished dough */
    public static MixingBowlJeiRecipe finish() {
        return new MixingBowlJeiRecipe(
                List.of(Ingredient.of(ModItems.INCOMPLETE_MOONCAKE_DOUGH.get())),
                new ItemStack(ModItems.MOONCAKE_DOUGH.get())
        );
    }
}
