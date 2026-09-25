package com.mooncake.client;

import com.mooncake.blockentity.MixingBowlBlockEntity;
import java.util.List;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;

public class MixingBowlRenderer extends OrderedItemBlockEntityRenderer<MixingBowlBlockEntity> {
    public MixingBowlRenderer(BlockEntityRendererProvider.Context context) {
        // Inner floor of the bowl is at y = 1/16
        super(context, 1.0F / 16.0F);
    }

    @Override
    protected List<ItemStack> displayItems(MixingBowlBlockEntity blockEntity) {
        return blockEntity.getDisplayItems();
    }

    @Override
    protected boolean animate(MixingBowlBlockEntity blockEntity) {
        return blockEntity.isMixing();
    }
}
