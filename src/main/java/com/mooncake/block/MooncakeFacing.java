package com.mooncake.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * {@code FACING} = direction of the rectangle's short side (nearest the placer).
 * Default model has the long axis along X (east–west) and short sides on east/west,
 * so south-facing short side needs Y=90.
 */
public final class MooncakeFacing {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private MooncakeFacing() {
    }

    public static BlockState placementState(BlockState defaultState, BlockPlaceContext context) {
        // Short side toward the player
        return defaultState.setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    /** Blockstate / BER Y rotation; south = short side toward south. */
    public static int yRotation(Direction facing) {
        return switch (facing) {
            case SOUTH -> 90;
            case WEST -> 180;
            case NORTH -> 270;
            case EAST -> 0;
            default -> 90;
        };
    }
}
