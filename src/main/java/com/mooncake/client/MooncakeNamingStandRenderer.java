package com.mooncake.client;

import com.mooncake.blockentity.MooncakeNamingStandBlockEntity;
import java.util.List;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;

public class MooncakeNamingStandRenderer extends OrderedItemBlockEntityRenderer<MooncakeNamingStandBlockEntity> {
    public MooncakeNamingStandRenderer(BlockEntityRendererProvider.Context context) {
        super(context, 12.0F / 16.0F);
    }

    @Override
    protected List<ItemStack> displayItems(MooncakeNamingStandBlockEntity blockEntity) {
        return blockEntity.getDisplayItems();
    }

    @Override
    protected boolean animate(MooncakeNamingStandBlockEntity blockEntity) {
        return false;
    }
}
