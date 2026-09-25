package com.mooncake.registry;

import com.mooncake.MooncakeMod;
import com.mooncake.recipe.CakeFromPiecesRecipe;
import com.mooncake.recipe.HardenedBlockFromCakesRecipe;
import com.mooncake.recipe.HardenedGearShapedRecipe;
import com.mooncake.recipe.MooncakeBakingRecipe;
import com.mooncake.recipe.MooncakeCampfireCookingRecipe;
import com.mooncake.recipe.MooncakeWaxingRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCookingSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MooncakeMod.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MooncakeBakingRecipe>> BAKING_SERIALIZER =
            SERIALIZERS.register("baking", () -> new SimpleCookingSerializer<>(
                    (group, category, ingredient, result, experience, cookingTime) ->
                            new MooncakeBakingRecipe(RecipeType.SMELTING, group, category, ingredient, result, experience, cookingTime),
                    200
            ));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MooncakeBakingRecipe>> SMOKING_SERIALIZER =
            SERIALIZERS.register("smoking", () -> new SimpleCookingSerializer<>(
                    (group, category, ingredient, result, experience, cookingTime) ->
                            new MooncakeBakingRecipe(RecipeType.SMOKING, group, category, ingredient, result, experience, cookingTime),
                    100
            ));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MooncakeCampfireCookingRecipe>> CAMPFIRE_BAKING_SERIALIZER =
            SERIALIZERS.register("campfire_baking", () -> new SimpleCookingSerializer<>(
                    MooncakeCampfireCookingRecipe::new,
                    600
            ));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<HardenedBlockFromCakesRecipe>> HARDENED_BLOCK_FROM_CAKES_SERIALIZER =
            SERIALIZERS.register("hardened_block_from_cakes", HardenedBlockFromCakesRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<HardenedGearShapedRecipe>> HARDENED_GEAR_SHAPED_SERIALIZER =
            SERIALIZERS.register("hardened_gear_shaped", HardenedGearShapedRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CakeFromPiecesRecipe>> CAKE_FROM_PIECES_SERIALIZER =
            SERIALIZERS.register("cake_from_pieces", CakeFromPiecesRecipe.Serializer::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<MooncakeWaxingRecipe>> WAXING_SERIALIZER =
            SERIALIZERS.register("waxing", MooncakeWaxingRecipe.Serializer::new);

    private ModRecipes() {
    }

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }
}
