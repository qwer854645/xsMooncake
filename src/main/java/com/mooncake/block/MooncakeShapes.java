package com.mooncake.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Mooncake collision (~10 disc, 4.4 tall with crust rim).
 * Piece shapes match {@code mooncake_piece_shape} + blockstate Y rotation.
 */
public final class MooncakeShapes {
    public static final VoxelShape SHAPE = Shapes.or(
            Block.box(5.0D, 0.0D, 3.0D, 11.0D, 4.4D, 13.0D),
            Block.box(3.0D, 0.0D, 5.0D, 13.0D, 4.4D, 11.0D),
            Block.box(3.75D, 0.0D, 3.75D, 12.25D, 4.4D, 12.25D)
    );

    /** facing=east (model y=0): NW quarter. */
    private static final VoxelShape PIECE_EAST = Shapes.or(
            Block.box(3.0D, 0.0D, 3.0D, 8.0D, 4.4D, 8.0D),
            Block.box(3.5D, 0.0D, 3.0D, 8.0D, 4.4D, 8.5D),
            Block.box(3.0D, 0.0D, 3.5D, 8.5D, 4.4D, 8.0D)
    );

    private static final VoxelShape PIECE_SOUTH = rotateY90(PIECE_EAST);
    private static final VoxelShape PIECE_WEST = rotateY90(PIECE_SOUTH);
    private static final VoxelShape PIECE_NORTH = rotateY90(PIECE_WEST);

    private MooncakeShapes() {
    }

    public static VoxelShape pieceShape(Direction facing) {
        return switch (facing) {
            case SOUTH -> PIECE_SOUTH;
            case WEST -> PIECE_WEST;
            case NORTH -> PIECE_NORTH;
            default -> PIECE_EAST;
        };
    }

    /**
     * Match blockstate model {@code "y": 90}: clockwise when viewed from above (N up),
     * so NW quarter → NE.
     */
    private static VoxelShape rotateY90(VoxelShape shape) {
        VoxelShape[] result = {Shapes.empty()};
        shape.forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
            result[0] = Shapes.or(
                    result[0],
                    Shapes.box(1.0D - maxZ, minY, minX, 1.0D - minZ, maxY, maxX)
            );
        });
        return result[0];
    }
}
