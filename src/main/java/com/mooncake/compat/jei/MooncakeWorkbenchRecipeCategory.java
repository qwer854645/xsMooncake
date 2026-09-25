package com.mooncake.compat.jei;

import com.mooncake.MooncakeMod;
import com.mooncake.registry.ModItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class MooncakeWorkbenchRecipeCategory implements IRecipeCategory<MooncakeWorkbenchJeiRecipe> {
    public static final RecipeType<MooncakeWorkbenchJeiRecipe> TYPE =
            RecipeType.create(MooncakeMod.MOD_ID, "mooncake_workbench", MooncakeWorkbenchJeiRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public MooncakeWorkbenchRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(130, 54);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.MOONCAKE_WORKBENCH.get()));
    }

    @Override
    public RecipeType<MooncakeWorkbenchJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mooncake.mooncake_workbench");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MooncakeWorkbenchJeiRecipe recipe, IFocusGroup focuses) {
        var inputs = recipe.inputs();
        for (int i = 0; i < inputs.size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 8 + i * 22, 18)
                    .addIngredients(inputs.get(i));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 100, 18)
                .addItemStack(recipe.output());
        builder.addSlot(RecipeIngredientRole.CATALYST, 54, 0)
                .addItemStack(new ItemStack(ModItems.MOONCAKE_WORKBENCH.get()));
    }

    @Override
    public void draw(
            MooncakeWorkbenchJeiRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics,
            double mouseX,
            double mouseY
    ) {
    }
}
