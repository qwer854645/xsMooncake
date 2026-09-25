package com.mooncake.mixin.farmersdelight;

import com.mooncake.block.MooncakeBlocks;
import com.mooncake.component.MooncakeCrust;
import com.mooncake.component.MooncakeFillings;
import com.mooncake.item.MooncakeBlockItem;
import com.mooncake.item.MooncakePieceItem;
import com.mooncake.registry.ModBlocks;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Soft / hardened cake → pieces: match weather variant and copy fillings onto each piece.
 */
@Mixin(targets = "vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe", remap = false)
public class CuttingBoardRecipeMixin {
    @Inject(method = "rollResults", at = @At("RETURN"), cancellable = true, remap = false)
    private void mooncake$preserveCakeData(
            RandomSource random,
            int fortuneLevel,
            RecipeWrapper inventory,
            CallbackInfoReturnable<List<ItemStack>> cir
    ) {
        List<ItemStack> results = cir.getReturnValue();
        if (results == null || results.isEmpty() || inventory == null) {
            return;
        }

        ItemStack input = inventory.getItem(0);
        if (input == null || input.isEmpty()) {
            return;
        }
        if (!(input.getItem() instanceof MooncakeBlockItem blockItem)
                || !MooncakeBlocks.isCuttableCake(blockItem.getBlock())) {
            return;
        }

        ItemStack pieceTemplate = new ItemStack(ModBlocks.pieceForCake(blockItem.getBlock()).asItem());
        MooncakeFillings fillings = MooncakeFillings.get(input);
        MooncakeCrust crust = MooncakeCrust.get(input);

        List<ItemStack> rewritten = new ArrayList<>(results.size());
        for (ItemStack result : results) {
            if (result == null || result.isEmpty()) {
                rewritten.add(result);
                continue;
            }
            ItemStack out;
            if (isPieceResult(result)) {
                out = pieceTemplate.copyWithCount(result.getCount());
            } else {
                out = result.copy();
            }
            if (!fillings.isEmpty()) {
                MooncakeFillings.set(out, fillings);
            }
            if (!crust.isEmpty()) {
                MooncakeCrust.set(out, crust);
            }
            MooncakeFillings.stripRedundantWeather(out);
            rewritten.add(out);
        }
        cir.setReturnValue(rewritten);
    }

    private static boolean isPieceResult(ItemStack stack) {
        if (stack.getItem() instanceof MooncakePieceItem) {
            return true;
        }
        return stack.getItem() instanceof BlockItem blockItem && MooncakeBlocks.isPiece(blockItem.getBlock());
    }
}
