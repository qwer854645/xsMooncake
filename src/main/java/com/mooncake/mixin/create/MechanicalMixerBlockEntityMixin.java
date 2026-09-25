package com.mooncake.mixin.create;

import com.mooncake.recipe.CakeFromPiecesRecipe;
import com.mooncake.recipe.HardenedBlockFromCakesRecipe;
import com.mooncake.recipe.HardenedGearShapedRecipe;
import com.mooncake.recipe.MooncakeWaxingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Widen mixer static filters only for mooncake custom recipes.
 * Never flips {@code true → false}, so other addons' allow-lists stay intact.
 */
@Mixin(targets = "com.simibubi.create.content.kinetics.mixer.MechanicalMixerBlockEntity", remap = false)
public class MechanicalMixerBlockEntityMixin {
    @Inject(method = "matchStaticFilters", at = @At("RETURN"), cancellable = true, remap = false)
    private void mooncake$allowDataRecipes(RecipeHolder<?> recipe, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            return;
        }
        Object value = recipe.value();
        if (value instanceof HardenedBlockFromCakesRecipe
                || value instanceof HardenedGearShapedRecipe
                || value instanceof CakeFromPiecesRecipe
                || value instanceof MooncakeWaxingRecipe) {
            cir.setReturnValue(true);
        }
    }
}
