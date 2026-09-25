package com.mooncake.util;

import com.mooncake.block.MooncakeBlocks;
import com.mooncake.item.MooncakeBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** Layout constants matching the voxel mooncake top frame (see rebuild_mooncake_base.py). */
public final class MooncakeInscriptionLayout {
    public static final float HEIGHT = 4.0F / 16.0F;
    public static final float RIM_EXTRA = 0.4F / 16.0F;
    public static final float PANEL_EXTRA = 0.22F / 16.0F;

    // Frame outer bounds in block space (pixels 9–30 x, 14–25 z @ cell 0.25, origin 3)
    public static final float FRAME_X0 = 5.25F / 16.0F;
    public static final float FRAME_X1 = 10.75F / 16.0F;
    public static final float FRAME_Z0 = 6.5F / 16.0F;
    public static final float FRAME_Z1 = 9.5F / 16.0F;

    // Interior (inside 2px border)
    public static final float PANEL_X0 = 5.75F / 16.0F;
    public static final float PANEL_X1 = 10.5F / 16.0F;
    public static final float PANEL_Z0 = 7.0F / 16.0F;
    public static final float PANEL_Z1 = 9.25F / 16.0F;

    private MooncakeInscriptionLayout() {
    }

    public static boolean isNameableMooncake(ItemStack stack) {
        if (!(stack.getItem() instanceof MooncakeBlockItem item)) {
            return false;
        }
        Block block = item.getBlock();
        return MooncakeBlocks.isMooncake(block) && !MooncakeBlocks.isPiece(block);
    }
}
