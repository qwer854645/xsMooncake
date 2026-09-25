package com.mooncake.block;

import com.mojang.serialization.MapCodec;
import com.mooncake.blockentity.MixingBowlBlockEntity;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Wooden bowl for two-step mooncake dough: base mix → incomplete; finish mix → dough. */
public class MixingBowlBlock extends BaseEntityBlock {
    public static final MapCodec<MixingBowlBlock> CODEC = simpleCodec(MixingBowlBlock::new);
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 6.0D, 14.0D);

    public MixingBowlBlock(Properties properties) {
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
        return new MixingBowlBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide
                ? null
                : createTickerHelper(type, ModBlockEntities.MIXING_BOWL.get(), MixingBowlBlockEntity::serverTick);
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
        if (!(level.getBlockEntity(pos) instanceof MixingBowlBlockEntity bowl)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            if (bowl.tryStartMixing()) {
                return ItemInteractionResult.CONSUME;
            }
            if (!stack.isEmpty() && bowl.tryInsert(stack, player)) {
                return ItemInteractionResult.CONSUME;
            }
            return ItemInteractionResult.CONSUME;
        }

        if (!stack.isEmpty()) {
            if (bowl.tryInsert(stack, player)) {
                return ItemInteractionResult.CONSUME;
            }
            return ItemInteractionResult.CONSUME;
        }

        if (bowl.tryExtract(player)) {
            return ItemInteractionResult.CONSUME;
        }
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MixingBowlBlockEntity bowl) {
            NonNullList<ItemStack> drops = NonNullList.create();
            drops.addAll(bowl.getDropStacks());
            Containers.dropContents(level, pos, drops);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
