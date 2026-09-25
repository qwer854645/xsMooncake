package com.mooncake.block;

import com.mooncake.blockentity.MooncakeBlockEntity;
import com.mooncake.registry.ModHardenedBlocks;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

/**
 * Place / break / wax / oxidize persistence for hardened building blocks.
 * Fillings + crust live on {@link MooncakeBlockEntity}, same as cakes.
 */
public final class HardenedFillingsBlock {
    private HardenedFillingsBlock() {
    }

    public static BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MooncakeBlockEntity(pos, state);
    }

    /**
     * Keep the BE across wax / dewax / oxidation ID swaps within the hardened family.
     */
    public static void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && !ModHardenedBlocks.isFamily(newState.getBlock())) {
            level.removeBlockEntity(pos);
        }
    }

    public static void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        if (level.isClientSide || !ModHardenedBlocks.isFamily(oldState.getBlock()) || oldState.is(state.getBlock())) {
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockPos immutablePos = pos.immutable();
        int when = serverLevel.getServer().getTickCount() + 1;
        serverLevel.getServer().tell(new TickTask(when, () -> {
            if (serverLevel.getBlockEntity(immutablePos) instanceof MooncakeBlockEntity be
                    && ModHardenedBlocks.isFamily(serverLevel.getBlockState(immutablePos).getBlock())) {
                be.syncToClients();
            }
        }));
    }

    public static void setPlacedBy(
            Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack
    ) {
        writeFromStack(level, pos, stack);
        // Door upper half is placed without its own setPlacedBy stack — mirror data.
        if (state.hasProperty(DoorBlock.HALF) && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
            writeFromStack(level, pos.above(), stack);
        }
    }

    public static ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        ItemStack stack = new ItemStack(state.getBlock().asItem());
        if (level.getBlockEntity(pos) instanceof MooncakeBlockEntity be) {
            be.writeToStack(stack);
        }
        return stack;
    }

    public static List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        // Other door half is cleared with no-drops; the mined half always drops once.
        ItemStack stack = new ItemStack(state.getBlock().asItem());
        if (state.hasProperty(SlabBlock.TYPE) && state.getValue(SlabBlock.TYPE) == SlabType.DOUBLE) {
            stack.setCount(2);
        }

        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof MooncakeBlockEntity mooncake) {
            mooncake.writeToStack(stack);
        }
        return List.of(stack);
    }

    private static void writeFromStack(Level level, BlockPos pos, ItemStack stack) {
        if (level.getBlockEntity(pos) instanceof MooncakeBlockEntity be) {
            be.setFromStack(stack);
        }
    }
}
