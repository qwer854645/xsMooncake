package com.mooncake.recipe;

import com.mooncake.registry.ModRecipes;
import com.mooncake.util.MooncakeDataTransfer;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.Blocks;

public class MooncakeBakingRecipe extends AbstractCookingRecipe {
    public MooncakeBakingRecipe(
            RecipeType<?> type,
            String group,
            CookingBookCategory category,
            Ingredient ingredient,
            ItemStack result,
            float experience,
            int cookingTime
    ) {
        super(type, group, category, ingredient, result, experience, cookingTime);
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        ItemStack out = this.result.copy();
        MooncakeDataTransfer.copyAll(input.item(), out);
        return out;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        if (this.type == RecipeType.SMOKING) {
            return ModRecipes.SMOKING_SERIALIZER.get();
        }
        return ModRecipes.BAKING_SERIALIZER.get();
    }

    @Override
    public ItemStack getToastSymbol() {
        if (this.type == RecipeType.SMOKING) {
            return new ItemStack(Blocks.SMOKER);
        }
        return new ItemStack(Blocks.FURNACE);
    }
}
