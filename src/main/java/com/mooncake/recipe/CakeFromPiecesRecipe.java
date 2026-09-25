package com.mooncake.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mooncake.block.MooncakeBlocks;
import com.mooncake.component.MooncakeCrust;
import com.mooncake.component.MooncakeFillings;
import com.mooncake.component.MooncakeWeather;
import com.mooncake.item.MooncakeBlockItem;
import com.mooncake.registry.ModBlocks;
import com.mooncake.registry.ModItems;
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
import net.minecraft.world.level.block.Block;

/**
 * 4 mooncake pieces (soft or hardened) of the same oxidation / wax → one whole cake.
 * Distinct fillings mix as「 加 」; identical piece fillings are not stacked ×n.
 */
public class CakeFromPiecesRecipe implements CraftingRecipe {
    private final CraftingBookCategory category;

    public CakeFromPiecesRecipe(CraftingBookCategory category) {
        this.category = category;
    }

    public CakeFromPiecesRecipe() {
        this(CraftingBookCategory.MISC);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        int pieces = 0;
        Boolean hardened = null;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (!(stack.getItem() instanceof MooncakeBlockItem blockItem)
                    || !MooncakeBlocks.isPiece(blockItem.getBlock())) {
                return false;
            }
            boolean pieceHardened = MooncakeBlocks.isHardenedCake(blockItem.getBlock());
            if (hardened == null) {
                hardened = pieceHardened;
            } else if (hardened != pieceHardened) {
                return false;
            }
            pieces++;
        }
        return pieces == 4 && MooncakeCraftWeather.allSame(input);
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        List<MooncakeFillings> fillingSources = new ArrayList<>();
        List<MooncakeCrust> crustSources = new ArrayList<>();
        boolean hardened = false;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof MooncakeBlockItem blockItem) {
                hardened = MooncakeBlocks.isHardenedCake(blockItem.getBlock());
            }
            fillingSources.add(MooncakeFillings.get(stack));
            crustSources.add(MooncakeCrust.get(stack));
        }
        MooncakeWeather weather = MooncakeCraftWeather.first(input);
        Block cake = ModBlocks.byWeather(weather.state(), weather.waxed(), hardened, false);
        ItemStack result = new ItemStack(cake.asItem());
        MooncakeFillings.set(result, MooncakeFillings.mixDistinctFromSources(fillingSources));
        MooncakeCrust.set(result, MooncakeCrust.mixDistinctFromSources(crustSources));
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 4;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(ModItems.MOONCAKE.get());
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        Ingredient piece = Ingredient.of(ModTags.MOONCAKE_PIECES);
        for (int i = 0; i < 4; i++) {
            list.add(piece);
        }
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.CAKE_FROM_PIECES_SERIALIZER.get();
    }

    @Override
    public CraftingBookCategory category() {
        return category;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static class Serializer implements RecipeSerializer<CakeFromPiecesRecipe> {
        public static final MapCodec<CakeFromPiecesRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC)
                        .forGetter(CakeFromPiecesRecipe::category)
        ).apply(instance, CakeFromPiecesRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, CakeFromPiecesRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        CraftingBookCategory.STREAM_CODEC,
                        CakeFromPiecesRecipe::category,
                        CakeFromPiecesRecipe::new
                );

        @Override
        public MapCodec<CakeFromPiecesRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CakeFromPiecesRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
