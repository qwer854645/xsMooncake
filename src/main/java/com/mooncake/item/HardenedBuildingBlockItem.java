package com.mooncake.item;

import com.mooncake.HardenedFillingsCraftHandler;
import com.mooncake.component.MooncakeFillings;
import com.mooncake.component.MooncakeWeather;
import com.mooncake.registry.ModHardenedBlocks;
import com.mooncake.util.MooncakeNames;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DoubleHighBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** Building-block items that show / keep mooncake fillings. */
public class HardenedBuildingBlockItem extends BlockItem {
    public enum Product {
        BLOCK("item.mooncake.filled_hardened_block", "item.mooncake.filled_hardened_block_truncated", "block.mooncake.hardened_mooncake_block"),
        CUT("item.mooncake.filled_hardened_cut", "item.mooncake.filled_hardened_cut_truncated", "block.mooncake.cut_hardened_mooncake"),
        SLAB("item.mooncake.filled_hardened_slab", "item.mooncake.filled_hardened_slab_truncated", "block.mooncake.cut_hardened_mooncake_slab"),
        STAIRS("item.mooncake.filled_hardened_stairs", "item.mooncake.filled_hardened_stairs_truncated", "block.mooncake.cut_hardened_mooncake_stairs"),
        DOOR("item.mooncake.filled_hardened_door", "item.mooncake.filled_hardened_door_truncated", "block.mooncake.hardened_mooncake_door");

        public final String filledKey;
        public final String truncatedKey;
        public final String emptyKey;

        Product(String filledKey, String truncatedKey, String emptyKey) {
            this.filledKey = filledKey;
            this.truncatedKey = truncatedKey;
            this.emptyKey = emptyKey;
        }
    }

    private final Product product;

    public HardenedBuildingBlockItem(Block block, Product product, Properties properties) {
        super(block, properties);
        this.product = product;
    }

    public Product product() {
        return product;
    }

    @Override
    public Component getName(ItemStack stack) {
        MooncakeFillings.stripRedundantWeather(stack);
        MooncakeFillings fillings = MooncakeFillings.get(stack);
        MooncakeWeather weather = ModHardenedBlocks.weatherOf(this.getBlock());
        if (fillings.isEmpty() && com.mooncake.component.MooncakeCrust.get(stack).isEmpty()) {
            return this.getBlock().getName();
        }
        return MooncakeNames.forStackProduct(
                stack,
                weather,
                product.filledKey,
                product.truncatedKey,
                product.emptyKey,
                true
        );
    }

    @Override
    public void onCraftedBy(ItemStack stack, Level level, Player player) {
        HardenedFillingsCraftHandler.copyFromStonecutter(stack, level, player);
        super.onCraftedBy(stack, level, player);
    }

    public static class Door extends DoubleHighBlockItem {
        private final Product product = Product.DOOR;

        public Door(Block block, Properties properties) {
            super(block, properties);
        }

        @Override
        public Component getName(ItemStack stack) {
            MooncakeFillings.stripRedundantWeather(stack);
            MooncakeFillings fillings = MooncakeFillings.get(stack);
            MooncakeWeather weather = ModHardenedBlocks.weatherOf(this.getBlock());
            if (fillings.isEmpty() && com.mooncake.component.MooncakeCrust.get(stack).isEmpty()) {
                return this.getBlock().getName();
            }
            return MooncakeNames.forStackProduct(
                    stack,
                    weather,
                    product.filledKey,
                    product.truncatedKey,
                    product.emptyKey,
                    true
            );
        }

        @Override
        public void onCraftedBy(ItemStack stack, Level level, Player player) {
            HardenedFillingsCraftHandler.copyFromStonecutter(stack, level, player);
            super.onCraftedBy(stack, level, player);
        }
    }
}
