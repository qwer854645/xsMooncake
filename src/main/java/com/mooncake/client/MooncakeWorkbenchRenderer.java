package com.mooncake.client;

import com.mooncake.blockentity.MooncakeWorkbenchBlockEntity;
import java.util.List;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;

public class MooncakeWorkbenchRenderer extends OrderedItemBlockEntityRenderer<MooncakeWorkbenchBlockEntity> {
    public MooncakeWorkbenchRenderer(BlockEntityRendererProvider.Context context) {
        // Workbench top ~12/16 high
        super(context, 0.82F);
    }

    @Override
    protected List<ItemStack> displayItems(MooncakeWorkbenchBlockEntity blockEntity) {
        return blockEntity.getDisplayItems();
    }

    @Override
    protected boolean animate(MooncakeWorkbenchBlockEntity blockEntity) {
        return false;
    }
}
