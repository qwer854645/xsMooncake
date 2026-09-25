package com.mooncake.mixin.create;

import com.mooncake.compat.create.CreateBasinCrafting;
import com.mooncake.compat.create.CreateMooncakeBasinOps;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Basin crafting: mooncake recipes that need {@code assemble} keep fillings/crust.
 * Also handles mixer finish-dough and press raw-mooncake marker recipes with extras.
 * All other basin recipes keep Create's default path.
 */
@Mixin(targets = "com.simibubi.create.content.processing.basin.BasinRecipe", remap = false)
public class BasinRecipeMixin {
    @Inject(
            method = "apply(Lcom/simibubi/create/content/processing/basin/BasinBlockEntity;Lnet/minecraft/world/item/crafting/Recipe;Z)Z",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void mooncake$preserveData(
            BasinBlockEntity basin,
            Recipe<?> recipe,
            boolean test,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Boolean special = CreateMooncakeBasinOps.tryApply(basin, recipe, test);
        if (special != null) {
            cir.setReturnValue(special);
            return;
        }
        if (recipe instanceof CraftingRecipe crafting) {
            Boolean handled = CreateBasinCrafting.tryApply(basin, crafting, test);
            if (handled != null) {
                cir.setReturnValue(handled);
            }
        }
    }
}
