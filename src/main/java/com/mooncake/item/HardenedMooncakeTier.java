package com.mooncake.item;

import com.mooncake.registry.ModTags;
import java.util.List;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/** Iron-tier hardened mooncake tools/weapons; repair with any hardened mooncake cake. */
public enum HardenedMooncakeTier implements Tier {
    INSTANCE;

    @Override
    public int getUses() {
        return 400;
    }

    @Override
    public float getSpeed() {
        return 6.5F;
    }

    @Override
    public float getAttackDamageBonus() {
        return 2.5F;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return BlockTags.INCORRECT_FOR_IRON_TOOL;
    }

    @Override
    public int getEnchantmentValue() {
        return 14;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return Ingredient.of(ModTags.HARDENED_MOONCAKE_CAKES);
    }
}
