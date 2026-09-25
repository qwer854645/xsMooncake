package com.mooncake.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mooncake.registry.ModItems;
import com.mooncake.registry.ModRecipes;
import com.mooncake.util.MooncakeItemVariants;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Shapeless: plastic wrap + any unwaxed mooncake-family item → matching wrapped item.
 * Preserves fillings / durability via {@link ItemStack#transmuteCopy}.
 */
public class MooncakeWaxingRecipe implements CraftingRecipe {
    private final CraftingBookCategory category;

    public MooncakeWaxingRecipe(CraftingBookCategory category) {
        this.category = category;
    }

    public MooncakeWaxingRecipe() {
        this(CraftingBookCategory.MISC);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return findTarget(input).isPresent();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return findTarget(input)
                .map(target -> {
                    Item waxed = MooncakeItemVariants.waxedVersion(target.getItem()).orElseThrow();
                    return MooncakeItemVariants.transmute(target, waxed);
                })
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(ModItems.PLASTIC_WRAP.get());
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(Ingredient.of(ModItems.PLASTIC_WRAP.get()));
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.WAXING_SERIALIZER.get();
    }

    @Override
    public CraftingBookCategory category() {
        return category;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    private static Optional<ItemStack> findTarget(CraftingInput input) {
        ItemStack target = ItemStack.EMPTY;
        boolean wrap = false;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(ModItems.PLASTIC_WRAP.get())) {
                if (wrap) {
                    return Optional.empty();
                }
                wrap = true;
                continue;
            }
            if (!target.isEmpty() || !MooncakeItemVariants.canWax(stack)) {
                return Optional.empty();
            }
            target = stack;
        }
        if (!wrap || target.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(target);
    }

    public static class Serializer implements RecipeSerializer<MooncakeWaxingRecipe> {
        public static final MapCodec<MooncakeWaxingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC)
                        .forGetter(MooncakeWaxingRecipe::category)
        ).apply(instance, MooncakeWaxingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, MooncakeWaxingRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        CraftingBookCategory.STREAM_CODEC,
                        MooncakeWaxingRecipe::category,
                        MooncakeWaxingRecipe::new
                );

        @Override
        public MapCodec<MooncakeWaxingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MooncakeWaxingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
