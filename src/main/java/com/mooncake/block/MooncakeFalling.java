package com.mooncake.block;

import com.mooncake.blockentity.MooncakeBlockEntity;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Shared sand-like falling for mooncake / hardened mooncake / pieces. */
public final class MooncakeFalling {
    private static final int DELAY = 2;

    private MooncakeFalling() {
    }

    public static void schedule(LevelAccessor level, BlockPos pos, Block block) {
        level.scheduleTick(pos, block, DELAY);
    }

    public static void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        schedule(level, pos, state.getBlock());
        MooncakeBlocks.onPlace(state, level, pos, oldState, moved);
    }

    public static BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        schedule(level, pos, state.getBlock());
        return state;
    }

    public static void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!FallingBlock.isFree(level.getBlockState(pos.below())) || pos.getY() < level.getMinBuildHeight()) {
            return;
        }

        CompoundTag beTag = captureBlockEntity(level, pos);
        FallingBlockEntity falling = FallingBlockEntity.fall(level, pos, state);
        if (beTag != null) {
            falling.blockData = beTag;
        }
    }

    @Nullable
    private static CompoundTag captureBlockEntity(ServerLevel level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof MooncakeBlockEntity)) {
            return null;
        }
        return be.saveWithoutMetadata(level.registryAccess());
    }
}
