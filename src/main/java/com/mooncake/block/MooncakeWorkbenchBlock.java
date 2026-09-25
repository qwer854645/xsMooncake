package com.mooncake.block;

import com.mojang.serialization.MapCodec;
import com.mooncake.blockentity.MooncakeWorkbenchBlockEntity;
import com.mooncake.registry.ModBlockEntities;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Assemble raw mooncakes: place dough then fillings; empty-hand take raw;
 * sneak-empty extract last placed item.
 */
public class MooncakeWorkbenchBlock extends BaseEntityBlock {
    public static final MapCodec<MooncakeWorkbenchBlock> CODEC = simpleCodec(MooncakeWorkbenchBlock::new);
    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D);

    public MooncakeWorkbenchBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MooncakeWorkbenchBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (!(level.getBlockEntity(pos) instanceof MooncakeWorkbenchBlockEntity bench)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            if (stack.isEmpty()) {
                bench.tryExtractLast(player);
            }
            return ItemInteractionResult.CONSUME;
        }

        if (!stack.isEmpty()) {
            bench.tryInsert(stack, player);
            return ItemInteractionResult.CONSUME;
        }

        if (bench.canTakeRaw()) {
            bench.tryTakeRaw(player);
        }
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MooncakeWorkbenchBlockEntity bench) {
            NonNullList<ItemStack> drops = NonNullList.create();
            drops.addAll(bench.getDropStacks());
            Containers.dropContents(level, pos, drops);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
