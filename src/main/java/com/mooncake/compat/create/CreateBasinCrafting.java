package com.mooncake.compat.create;

import com.mooncake.recipe.CakeFromPiecesRecipe;
import com.mooncake.recipe.HardenedBlockFromCakesRecipe;
import com.mooncake.recipe.HardenedGearShapedRecipe;
import com.mooncake.recipe.MooncakeWaxingRecipe;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Runs mooncake {@link CraftingRecipe}s inside a Create basin using {@code assemble},
 * so fillings / custom data are never dropped (Create's default path only uses getResultItem).
 */
public final class CreateBasinCrafting {
    private CreateBasinCrafting() {
    }

    /** Only these types need assemble; every other crafting recipe stays on Create defaults. */
    private static boolean needsAssemble(CraftingRecipe recipe) {
        return recipe instanceof HardenedBlockFromCakesRecipe
                || recipe instanceof HardenedGearShapedRecipe
                || recipe instanceof CakeFromPiecesRecipe
                || recipe instanceof MooncakeWaxingRecipe;
    }

    /**
     * @return {@code null} if this recipe should use Create's default logic;
     *         otherwise success/failure of our custom apply.
     */
    public static Boolean tryApply(Object basin, CraftingRecipe recipe, boolean test) {
        if (!needsAssemble(recipe)) {
            return null;
        }

        if (!(basin instanceof BlockEntity be) || be.getLevel() == null) {
            return false;
        }
        Level level = be.getLevel();
        IItemHandler items = level.getCapability(Capabilities.ItemHandler.BLOCK, be.getBlockPos(), null);
        if (items == null) {
            return false;
        }

        List<Ingredient> ingredients = new LinkedList<>(recipe.getIngredients());
        if (ingredients.isEmpty()) {
            return false;
        }

        List<ItemStack> outputs = new ArrayList<>();

        for (boolean simulate : new boolean[]{true, false}) {
            if (!simulate && test) {
                return true;
            }

            int[] extractedItemsFromSlot = new int[items.getSlots()];
            List<ItemStack> consumed = new ArrayList<>();

            Ingredients:
            for (Ingredient ingredient : ingredients) {
                if (ingredient.isEmpty()) {
                    continue;
                }
                for (int slot = 0; slot < items.getSlots(); slot++) {
                    if (simulate && items.getStackInSlot(slot).getCount() <= extractedItemsFromSlot[slot]) {
                        continue;
                    }
                    ItemStack peek = items.extractItem(slot, 1, true);
                    if (!ingredient.test(peek)) {
                        continue;
                    }
                    ItemStack taken = simulate ? peek.copyWithCount(1) : items.extractItem(slot, 1, false);
                    extractedItemsFromSlot[slot]++;
                    consumed.add(taken.copyWithCount(1));
                    continue Ingredients;
                }
                return false;
            }

            if (simulate) {
                ItemStack assembled = assemble(recipe, consumed, level);
                outputs.clear();
                outputs.add(assembled);
                if (!accept(basin, outputs, true)) {
                    return false;
                }
            } else {
                ItemStack assembled = assemble(recipe, consumed, level);
                outputs.clear();
                outputs.add(assembled);
                return accept(basin, outputs, false);
            }
        }
        return false;
    }

    private static ItemStack assemble(CraftingRecipe recipe, List<ItemStack> consumed, Level level) {
        CraftingInput input = asInput(consumed);
        ItemStack assembled = recipe.assemble(input, level.registryAccess());
        if (assembled.isEmpty()) {
            assembled = recipe.getResultItem(level.registryAccess()).copy();
        }
        CreateMooncakeData.mergeFillingsFrom(consumed, assembled);
        for (ItemStack stack : consumed) {
            CreateMooncakeData.transfer(stack, assembled);
        }
        return assembled;
    }

    private static CraftingInput asInput(List<ItemStack> consumed) {
        int size = Math.max(9, consumed.size());
        NonNullList<ItemStack> grid = NonNullList.withSize(size, ItemStack.EMPTY);
        for (int i = 0; i < consumed.size(); i++) {
            grid.set(i, consumed.get(i).copy());
        }
        int width = size <= 4 ? 2 : 3;
        int height = Math.max(1, (size + width - 1) / width);
        return CraftingInput.of(width, height, grid);
    }

    private static boolean accept(Object basin, List<ItemStack> results, boolean simulate) {
        try {
            var method = basin.getClass().getMethod("acceptOutputs", List.class, List.class, boolean.class);
            Object ok = method.invoke(basin, results, Collections.emptyList(), simulate);
            return Boolean.TRUE.equals(ok);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}
