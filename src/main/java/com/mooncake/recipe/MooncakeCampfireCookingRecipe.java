package com.mooncake.recipe;

import com.mooncake.registry.ModRecipes;
import com.mooncake.util.MooncakeDataTransfer;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;

/** Campfire cooking that preserves mooncake fillings and crust. Must extend {@link CampfireCookingRecipe}. */
public class MooncakeCampfireCookingRecipe extends CampfireCookingRecipe {
    public MooncakeCampfireCookingRecipe(
            String group,
            CookingBookCategory category,
            Ingredient ingredient,
            ItemStack result,
            float experience,
            int cookingTime
    ) {
        super(group, category, ingredient, result, experience, cookingTime);
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        ItemStack out = this.result.copy();
        MooncakeDataTransfer.copyAll(input.item(), out);
        return out;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CAMPFIRE_BAKING_SERIALIZER.get();
    }
}
