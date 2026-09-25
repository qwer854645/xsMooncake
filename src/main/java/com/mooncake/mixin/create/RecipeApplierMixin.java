package com.mooncake.mixin.create;

import com.mooncake.compat.create.CreateMooncakeData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ensure Create processing (press / fan smoking / blasting / belts) keeps mooncake data.
 * Scoped only to mooncake-related stacks so other mods keep Create's default path.
 */
@Mixin(targets = "com.simibubi.create.foundation.recipe.RecipeApplier", remap = false)
public class RecipeApplierMixin {
    @Inject(
            method = "applyRecipeOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/crafting/Recipe;Z)Ljava/util/List;",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void mooncake$cookingAssemble(
            Level level,
            ItemStack stackIn,
            Recipe<?> recipe,
            boolean returnProcessingRemainder,
            CallbackInfoReturnable<List<ItemStack>> cir
    ) {
        if (!(recipe instanceof AbstractCookingRecipe cooking) || stackIn == null || stackIn.isEmpty()) {
            return;
        }
        ItemStack preview = cooking.getResultItem(level.registryAccess());
        if (!CreateMooncakeData.shouldRewriteCooking(stackIn, preview)) {
            return;
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < stackIn.getCount(); i++) {
            ItemStack assembled = cooking.assemble(
                    new SingleRecipeInput(stackIn.copyWithCount(1)),
                    level.registryAccess()
            );
            if (assembled.isEmpty()) {
                assembled = preview.copy();
            }
            CreateMooncakeData.transfer(stackIn, assembled);
            mergeInto(stacks, assembled);
        }
        if (returnProcessingRemainder && stackIn.hasCraftingRemainingItem()) {
            mergeInto(stacks, stackIn.getCraftingRemainingItem());
        }
        cir.setReturnValue(stacks);
    }

    @Inject(
            method = "applyRecipeOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/crafting/Recipe;Z)Ljava/util/List;",
            at = @At("RETURN"),
            remap = false
    )
    private static void mooncake$keepData(
            Level level,
            ItemStack stackIn,
            Recipe<?> recipe,
            boolean returnProcessingRemainder,
            CallbackInfoReturnable<List<ItemStack>> cir
    ) {
        if (!CreateMooncakeData.involvesMooncake(stackIn)) {
            return;
        }
        List<ItemStack> results = cir.getReturnValue();
        if (results == null || results.isEmpty()) {
            return;
        }
        CreateMooncakeData.transferToAll(stackIn, results);
    }

    private static void mergeInto(List<ItemStack> stacks, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        for (ItemStack existing : stacks) {
            if (ItemStack.isSameItemSameComponents(existing, stack)) {
                int move = Math.min(existing.getMaxStackSize() - existing.getCount(), stack.getCount());
                existing.grow(move);
                stack.shrink(move);
                if (stack.isEmpty()) {
                    return;
                }
            }
        }
        if (!stack.isEmpty()) {
            stacks.add(stack);
        }
    }
}
