package com.mooncake.compat.jei;

import com.mooncake.registry.ModItems;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

/** Synthetic JEI recipe for the mooncake workbench. */
public record MooncakeWorkbenchJeiRecipe(List<Ingredient> inputs, ItemStack output) {
    public static MooncakeWorkbenchJeiRecipe example() {
        return new MooncakeWorkbenchJeiRecipe(
                List.of(
                        Ingredient.of(ModItems.MOONCAKE_DOUGH.get()),
                        Ingredient.of(Items.SWEET_BERRIES)
                ),
                new ItemStack(ModItems.RAW_MOONCAKE.get())
        );
    }
}
