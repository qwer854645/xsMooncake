package com.mooncake.block;

import com.mooncake.blockentity.MooncakeBlockEntity;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Fallable;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WaxedMooncakeBlock extends Block implements EntityBlock, Fallable {
    private final WeatheringCopper.WeatherState weatherState;
    private final boolean hardened;
    private final boolean piece;

    public WaxedMooncakeBlock(WeatheringCopper.WeatherState weatherState, Properties properties) {
        this(weatherState, properties, false, false);
    }

    public WaxedMooncakeBlock(WeatheringCopper.WeatherState weatherState, Properties properties, boolean hardened) {
        this(weatherState, properties, hardened, false);
    }

    public WaxedMooncakeBlock(
            WeatheringCopper.WeatherState weatherState, Properties properties, boolean hardened, boolean piece
    ) {
        super(properties);
        this.weatherState = weatherState;
        this.hardened = hardened;
        this.piece = piece;
        this.registerDefaultState(this.stateDefinition.any().setValue(MooncakeFacing.FACING, Direction.SOUTH));
    }

    public WeatheringCopper.WeatherState getWeatherState() {
        return weatherState;
    }

    public boolean isHardened() {
        return hardened;
    }

    public boolean isPiece() {
        return piece;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MooncakeFacing.FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return MooncakeFacing.placementState(this.defaultBlockState(), context);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(MooncakeFacing.FACING, rotation.rotate(state.getValue(MooncakeFacing.FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(MooncakeFacing.FACING)));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return piece
                ? MooncakeShapes.pieceShape(state.getValue(MooncakeFacing.FACING))
                : MooncakeShapes.SHAPE;
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
        ItemInteractionResult axe = MooncakeAxeScrape.useAxeOnBlock(stack, state, level, pos, player, hand);
        if (axe.consumesAction()) {
            return axe;
        }
        return MooncakeWaterOxidation.useWaterBottleOnBlock(stack, state, level, pos, player, hand);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MooncakeBlockEntity(pos, state);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        MooncakeFalling.tick(state, level, pos, random);
    }

    @Override
    protected BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        MooncakeFalling.schedule(level, pos, this);
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        MooncakeBlocks.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        MooncakeFalling.onPlace(state, level, pos, oldState, movedByPiston);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof MooncakeBlockEntity be) {
            be.setFromStack(stack);
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return MooncakeBlocks.createItemStack(level, pos, state);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        ItemStack stack = new ItemStack(asItem());
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (be instanceof MooncakeBlockEntity mooncake) {
            mooncake.writeToStack(stack);
        }
        return List.of(stack);
    }
}
