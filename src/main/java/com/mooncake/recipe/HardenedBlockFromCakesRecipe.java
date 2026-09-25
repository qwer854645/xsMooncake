package com.mooncake.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mooncake.component.MooncakeCrust;
import com.mooncake.component.MooncakeFillings;
import com.mooncake.component.MooncakeWeather;
import com.mooncake.registry.ModHardenedBlocks;
import com.mooncake.registry.ModRecipes;
import com.mooncake.registry.ModTags;
import com.mooncake.util.MooncakeCraftWeather;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * 4 hardened mooncake cakes of the same oxidation / wax → matching hardened block.
 * Fillings merge as「 加 」groups.
 */
public class HardenedBlockFromCakesRecipe implements CraftingRecipe {
    private final CraftingBookCategory category;

    public HardenedBlockFromCakesRecipe(CraftingBookCategory category) {
        this.category = category;
    }

    public HardenedBlockFromCakesRecipe() {
        this(CraftingBookCategory.BUILDING);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int cakes = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (!stack.is(ModTags.HARDENED_MOONCAKE_CAKES)) {
                return false;
            }
            cakes++;
        }
        return cakes == 4 && MooncakeCraftWeather.allSame(input);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        List<MooncakeFillings> fillingSources = new ArrayList<>();
        List<MooncakeCrust> crustSources = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            fillingSources.add(MooncakeFillings.get(stack));
            crustSources.add(MooncakeCrust.get(stack));
        }
        MooncakeWeather weather = MooncakeCraftWeather.first(input);
        ItemStack result = new ItemStack(ModHardenedBlocks.blockFor(weather));
        MooncakeFillings.set(result, MooncakeFillings.combineFromSources(fillingSources));
        MooncakeCrust.set(result, MooncakeCrust.combineFromSources(crustSources));
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 4;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(ModHardenedBlocks.UNAFFECTED.block().get());
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        Ingredient cake = Ingredient.of(ModTags.HARDENED_MOONCAKE_CAKES);
        for (int i = 0; i < 4; i++) {
            list.add(cake);
        }
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.HARDENED_BLOCK_FROM_CAKES_SERIALIZER.get();
    }

    @Override
    public CraftingBookCategory category() {
        return category;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<HardenedBlockFromCakesRecipe> {
        public static final MapCodec<HardenedBlockFromCakesRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.BUILDING)
                        .forGetter(HardenedBlockFromCakesRecipe::category)
        ).apply(instance, HardenedBlockFromCakesRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, HardenedBlockFromCakesRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        CraftingBookCategory.STREAM_CODEC,
                        HardenedBlockFromCakesRecipe::category,
                        HardenedBlockFromCakesRecipe::new
                );

        @Override
        public MapCodec<HardenedBlockFromCakesRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, HardenedBlockFromCakesRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
