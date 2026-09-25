package com.mooncake.registry;

import com.mooncake.MooncakeMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    public static final TagKey<Item> VALID_FILLINGS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "valid_fillings"));
    public static final TagKey<Item> BANNED_FILLINGS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "banned_fillings"));
    public static final TagKey<Item> VALID_CRUSTS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "valid_crusts"));
    public static final TagKey<Item> BANNED_CRUSTS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "banned_crusts"));
    public static final TagKey<Item> DOUGHS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "doughs"));
    public static final TagKey<Item> MINERAL_OR_STONE_FILLINGS =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "mineral_or_stone_fillings"));
    public static final TagKey<Item> WOOL_FILLINGS =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "wool_fillings"));
    public static final TagKey<Item> SCULK_FILLINGS =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "sculk_fillings"));
    public static final TagKey<Item> HARDENED_MOONCAKE_CAKES =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "hardened_mooncake_cakes"));
    public static final TagKey<Item> SOFT_MOONCAKE_CAKES =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "soft_mooncake_cakes"));
    public static final TagKey<Item> MOONCAKE_PIECES =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "mooncake_pieces"));
    public static final TagKey<Block> MOONCAKE_BLOCKS = BlockTags.create(ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "mooncake_blocks"));

    private ModTags() {
    }
}
