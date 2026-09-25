package com.mooncake.compat.jei;

import com.mooncake.MooncakeMod;
import com.mooncake.registry.ModItems;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@JeiPlugin
public class MooncakeJeiPlugin implements IModPlugin {
    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var helper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new MixingBowlRecipeCategory(helper),
                new MooncakeWorkbenchRecipeCategory(helper)
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(
                MixingBowlRecipeCategory.TYPE,
                List.of(MixingBowlJeiRecipe.incomplete(), MixingBowlJeiRecipe.finish())
        );
        registration.addRecipes(
                MooncakeWorkbenchRecipeCategory.TYPE,
                List.of(MooncakeWorkbenchJeiRecipe.example())
        );
        registration.addIngredientInfo(
                new ItemStack(ModItems.MIXING_BOWL.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.mooncake.mixing_bowl.info")
        );
        registration.addIngredientInfo(
                new ItemStack(ModItems.MOONCAKE_WORKBENCH.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.mooncake.mooncake_workbench.info")
        );
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModItems.MIXING_BOWL.get()), MixingBowlRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(ModItems.MOONCAKE_WORKBENCH.get()), MooncakeWorkbenchRecipeCategory.TYPE);
    }
}
