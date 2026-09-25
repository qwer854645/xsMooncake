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

public class MixingBowlRecipeCategory implements IRecipeCategory<MixingBowlJeiRecipe> {
    public static final RecipeType<MixingBowlJeiRecipe> TYPE =
            RecipeType.create(MooncakeMod.MOD_ID, "mixing_bowl", MixingBowlJeiRecipe.class);
    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "mixing_bowl");

    private final IDrawable background;
    private final IDrawable icon;

    public MixingBowlRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(150, 54);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModItems.MIXING_BOWL.get()));
    }

    @Override
    public RecipeType<MixingBowlJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.mooncake.mixing_bowl");
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
    public void setRecipe(IRecipeLayoutBuilder builder, MixingBowlJeiRecipe recipe, IFocusGroup focuses) {
        var inputs = recipe.inputs();
        for (int i = 0; i < inputs.size(); i++) {
            builder.addSlot(RecipeIngredientRole.INPUT, 8 + i * 22, 18)
                    .addIngredients(inputs.get(i));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 122, 18)
                .addItemStack(recipe.output());
        builder.addSlot(RecipeIngredientRole.CATALYST, 68, 0)
                .addItemStack(new ItemStack(ModItems.MIXING_BOWL.get()));
    }

    @Override
    public void draw(
            MixingBowlJeiRecipe recipe,
            IRecipeSlotsView recipeSlotsView,
            GuiGraphics guiGraphics,
            double mouseX,
            double mouseY
    ) {
        // Layout only — info text is shown via recipe tooltip / category title.
    }
}
