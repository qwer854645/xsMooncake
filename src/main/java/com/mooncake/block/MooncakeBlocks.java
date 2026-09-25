package com.mooncake.block;

import com.mooncake.blockentity.MooncakeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class MooncakeBlocks {
    private MooncakeBlocks() {
    }

    public static boolean isMooncake(Block block) {
        return block instanceof WeatheringMooncakeBlock || block instanceof WaxedMooncakeBlock;
    }

    public static boolean isMooncake(BlockState state) {
        return isMooncake(state.getBlock());
    }

    public static boolean isHardenedCake(Block block) {
        if (block instanceof WeatheringMooncakeBlock weathering) {
            return weathering.isHardened();
        }
        if (block instanceof WaxedMooncakeBlock waxed) {
            return waxed.isHardened();
        }
        return false;
    }

    public static boolean isPiece(Block block) {
        if (block instanceof WeatheringMooncakeBlock weathering) {
            return weathering.isPiece();
        }
        if (block instanceof WaxedMooncakeBlock waxed) {
            return waxed.isPiece();
        }
        return false;
    }

    /** Soft (non-hardened) whole cakes — knife cutting board. */
    public static boolean isSoftCake(Block block) {
        return isMooncake(block) && !isHardenedCake(block) && !isPiece(block);
    }

    /** Hardened whole cakes — axe cutting board. */
    public static boolean isHardenedWholeCake(Block block) {
        return isMooncake(block) && isHardenedCake(block) && !isPiece(block);
    }

    /** Whole cake eligible for cutting-board pieces. */
    public static boolean isCuttableCake(Block block) {
        return isSoftCake(block) || isHardenedWholeCake(block);
    }

    /**
     * Vanilla {@code BlockBehaviour.onRemove} drops the BE whenever the block ID changes.
     * Wax / dewax / oxidation swap between our 8 IDs, so keep the shared BE in those cases.
     */
    public static void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && !isMooncake(newState)) {
            level.removeBlockEntity(pos);
        }
    }

    /**
     * Clients always recreate the BE when the block ID changes; push fillings after a variant swap.
     * Delayed one tick so the packet arrives after the client's block-update recreation.
     */
    public static void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        if (level.isClientSide || !isMooncake(oldState) || oldState.is(state.getBlock())) {
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos immutablePos = pos.immutable();
        int when = serverLevel.getServer().getTickCount() + 1;
        serverLevel.getServer().tell(new TickTask(when, () -> {
            if (serverLevel.getBlockEntity(immutablePos) instanceof MooncakeBlockEntity be
                    && isMooncake(serverLevel.getBlockState(immutablePos))) {
                be.syncToClients();
            }
        }));
    }

    public static ItemStack createItemStack(LevelReader level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        ItemStack stack = new ItemStack(block.asItem());
        if (level.getBlockEntity(pos) instanceof MooncakeBlockEntity be) {
            be.writeToStack(stack);
        }
        return stack;
    }
}
