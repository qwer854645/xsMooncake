package com.mooncake.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mooncake.registry.ModRecipes;
import com.mooncake.util.MooncakeCraftWeather;
import com.mooncake.util.MooncakeDataTransfer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

/**
 * Shaped crafting for hardened gear that merges mooncake fillings in {@link #assemble}.
 * All mooncake ingredients must share the same oxidation / wax (and match the result).
 */
public class HardenedGearShapedRecipe extends ShapedRecipe {
    public HardenedGearShapedRecipe(
            String group,
            CraftingBookCategory category,
            ShapedRecipePattern pattern,
            ItemStack result,
            boolean showNotification
    ) {
        super(group, category, pattern, result, showNotification);
    }

    public HardenedGearShapedRecipe(
            String group,
            CraftingBookCategory category,
            ShapedRecipePattern pattern,
            ItemStack result
    ) {
        this(group, category, pattern, result, true);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (!super.matches(input, level)) {
            return false;
        }
        if (!MooncakeCraftWeather.allSame(input)) {
            return false;
        }
        ItemStack preview = getResultItem(level.registryAccess());
        if (!MooncakeCraftWeather.isWeatherBearing(preview)) {
            return true;
        }
        return MooncakeCraftWeather.first(input).equals(MooncakeCraftWeather.fromStack(preview));
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack result = super.assemble(input, registries);
        List<ItemStack> sources = new ArrayList<>();
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) {
                sources.add(stack);
            }
        }
        MooncakeDataTransfer.mergeFromSources(sources, result);
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.HARDENED_GEAR_SHAPED_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<HardenedGearShapedRecipe> {
        public static final MapCodec<HardenedGearShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(HardenedGearShapedRecipe::getGroup),
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.EQUIPMENT)
                        .forGetter(HardenedGearShapedRecipe::category),
                ShapedRecipePattern.MAP_CODEC.forGetter(recipe -> recipe.pattern),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.getResultItem(null)),
                Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(HardenedGearShapedRecipe::showNotification)
        ).apply(instance, HardenedGearShapedRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, HardenedGearShapedRecipe> STREAM_CODEC =
                StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

        @Override
        public MapCodec<HardenedGearShapedRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, HardenedGearShapedRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static HardenedGearShapedRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            ShapedRecipePattern pattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
            ItemStack result = ItemStack.STREAM_CODEC.decode(buffer);
            boolean showNotification = buffer.readBoolean();
            return new HardenedGearShapedRecipe(group, category, pattern, result, showNotification);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, HardenedGearShapedRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            buffer.writeEnum(recipe.category());
            ShapedRecipePattern.STREAM_CODEC.encode(buffer, recipe.pattern);
            ItemStack.STREAM_CODEC.encode(buffer, recipe.getResultItem(null));
            buffer.writeBoolean(recipe.showNotification());
        }
    }
}
