package com.mooncake.block;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopperDoorBlock;
import net.minecraft.world.level.block.WeatheringCopperFullBlock;
import net.minecraft.world.level.block.WeatheringCopperSlabBlock;
import net.minecraft.world.level.block.WeatheringCopperStairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Hardened building-block variants that keep fillings on a {@link com.mooncake.blockentity.MooncakeBlockEntity}.
 */
public final class HardenedBuildingBlockTypes {
    private HardenedBuildingBlockTypes() {
    }

    public static class WeatheringFull extends WeatheringCopperFullBlock implements EntityBlock {
        public WeatheringFull(WeatheringCopper.WeatherState weatherState, Properties properties) {
            super(weatherState, properties);
        }

        @Override
        protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
            this.changeOverTime(state, level, pos, random);
        }

        @Nullable
        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return HardenedFillingsBlock.newBlockEntity(pos, state);
        }

        @Override
        protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
            HardenedFillingsBlock.onRemove(state, level, pos, newState, moved);
        }

        @Override
        protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
            HardenedFillingsBlock.onPlace(state, level, pos, oldState, moved);
        }

        @Override
        public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
            super.setPlacedBy(level, pos, state, placer, stack);
            HardenedFillingsBlock.setPlacedBy(level, pos, state, placer, stack);
        }

        @Override
        public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
            return HardenedFillingsBlock.getCloneItemStack(level, pos, state);
        }

        @Override
        protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
            return HardenedFillingsBlock.getDrops(state, params);
        }

        @Override
        protected ItemInteractionResult useItemOn(
                ItemStack stack, BlockState state, Level level, BlockPos pos,
                Player player, InteractionHand hand, BlockHitResult hit
        ) {
            ItemInteractionResult axe = MooncakeAxeScrape.useAxeOnBlock(stack, state, level, pos, player, hand);
            if (axe.consumesAction()) {
                return axe;
            }
            return MooncakeWaterOxidation.useWaterBottleOnBlock(stack, state, level, pos, player, hand);
        }
    }

    public static class WaxedFull extends Block implements EntityBlock {
        public WaxedFull(Properties properties) {
            super(properties);
        }

        @Nullable
        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return HardenedFillingsBlock.newBlockEntity(pos, state);
        }

        @Override
        protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
            HardenedFillingsBlock.onRemove(state, level, pos, newState, moved);
        }

        @Override
        protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
            HardenedFillingsBlock.onPlace(state, level, pos, oldState, moved);
        }

        @Override
        public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
            super.setPlacedBy(level, pos, state, placer, stack);
            HardenedFillingsBlock.setPlacedBy(level, pos, state, placer, stack);
        }

        @Override
        public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
            return HardenedFillingsBlock.getCloneItemStack(level, pos, state);
        }

        @Override
        protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
            return HardenedFillingsBlock.getDrops(state, params);
        }

        @Override
        protected ItemInteractionResult useItemOn(
                ItemStack stack, BlockState state, Level level, BlockPos pos,
                Player player, InteractionHand hand, BlockHitResult hit
        ) {
            ItemInteractionResult axe = MooncakeAxeScrape.useAxeOnBlock(stack, state, level, pos, player, hand);
            if (axe.consumesAction()) {
                return axe;
            }
            return MooncakeWaterOxidation.useWaterBottleOnBlock(stack, state, level, pos, player, hand);
        }
    }

    public static class WeatheringSlab extends WeatheringCopperSlabBlock implements EntityBlock {
        public WeatheringSlab(WeatheringCopper.WeatherState weatherState, Properties properties) {
            super(weatherState, properties);
        }

        @Nullable
        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return HardenedFillingsBlock.newBlockEntity(pos, state);
        }

        @Override
        protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
            HardenedFillingsBlock.onRemove(state, level, pos, newState, moved);
        }

        @Override
        protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
            HardenedFillingsBlock.onPlace(state, level, pos, oldState, moved);
        }

        @Override
        public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
            super.setPlacedBy(level, pos, state, placer, stack);
            HardenedFillingsBlock.setPlacedBy(level, pos, state, placer, stack);
        }

        @Override
        public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
            return HardenedFillingsBlock.getCloneItemStack(level, pos, state);
        }

        @Override
        protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
            return HardenedFillingsBlock.getDrops(state, params);
        }

        @Override
        protected ItemInteractionResult useItemOn(
                ItemStack stack, BlockState state, Level level, BlockPos pos,
                Player player, InteractionHand hand, BlockHitResult hit
        ) {
            ItemInteractionResult axe = MooncakeAxeScrape.useAxeOnBlock(stack, state, level, pos, player, hand);
            if (axe.consumesAction()) {
                return axe;
            }
            return MooncakeWaterOxidation.useWaterBottleOnBlock(stack, state, level, pos, player, hand);
        }
    }

    public static class WaxedSlab extends SlabBlock implements EntityBlock {
        public WaxedSlab(Properties properties) {
            super(properties);
        }

        @Nullable
        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return HardenedFillingsBlock.newBlockEntity(pos, state);
        }

        @Override
        protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
            HardenedFillingsBlock.onRemove(state, level, pos, newState, moved);
        }

        @Override
        protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
            HardenedFillingsBlock.onPlace(state, level, pos, oldState, moved);
        }

        @Override
        public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
            super.setPlacedBy(level, pos, state, placer, stack);
            HardenedFillingsBlock.setPlacedBy(level, pos, state, placer, stack);
        }

        @Override
        public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
            return HardenedFillingsBlock.getCloneItemStack(level, pos, state);
        }

        @Override
        protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
            return HardenedFillingsBlock.getDrops(state, params);
        }

        @Override
        protected ItemInteractionResult useItemOn(
                ItemStack stack, BlockState state, Level level, BlockPos pos,
                Player player, InteractionHand hand, BlockHitResult hit
        ) {
            ItemInteractionResult axe = MooncakeAxeScrape.useAxeOnBlock(stack, state, level, pos, player, hand);
            if (axe.consumesAction()) {
                return axe;
            }
            return MooncakeWaterOxidation.useWaterBottleOnBlock(stack, state, level, pos, player, hand);
        }
    }

    public static class WeatheringStairs extends WeatheringCopperStairBlock implements EntityBlock {
        public WeatheringStairs(WeatheringCopper.WeatherState weatherState, BlockState base, Properties properties) {
            super(weatherState, base, properties);
        }

        @Nullable
        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return HardenedFillingsBlock.newBlockEntity(pos, state);
        }

        @Override
        protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
            HardenedFillingsBlock.onRemove(state, level, pos, newState, moved);
        }

        @Override
        protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
            HardenedFillingsBlock.onPlace(state, level, pos, oldState, moved);
        }

        @Override
        public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
            super.setPlacedBy(level, pos, state, placer, stack);
            HardenedFillingsBlock.setPlacedBy(level, pos, state, placer, stack);
        }

        @Override
        public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
            return HardenedFillingsBlock.getCloneItemStack(level, pos, state);
        }

        @Override
        protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
            return HardenedFillingsBlock.getDrops(state, params);
        }

        @Override
        protected ItemInteractionResult useItemOn(
                ItemStack stack, BlockState state, Level level, BlockPos pos,
                Player player, InteractionHand hand, BlockHitResult hit
        ) {
            ItemInteractionResult axe = MooncakeAxeScrape.useAxeOnBlock(stack, state, level, pos, player, hand);
            if (axe.consumesAction()) {
                return axe;
            }
            return MooncakeWaterOxidation.useWaterBottleOnBlock(stack, state, level, pos, player, hand);
        }
    }

    public static class WaxedStairs extends StairBlock implements EntityBlock {
        public WaxedStairs(BlockState base, Properties properties) {
            super(base, properties);
        }

        @Nullable
        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return HardenedFillingsBlock.newBlockEntity(pos, state);
        }

        @Override
        protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
            HardenedFillingsBlock.onRemove(state, level, pos, newState, moved);
        }

        @Override
        protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
            HardenedFillingsBlock.onPlace(state, level, pos, oldState, moved);
        }

        @Override
        public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
            super.setPlacedBy(level, pos, state, placer, stack);
            HardenedFillingsBlock.setPlacedBy(level, pos, state, placer, stack);
        }

        @Override
        public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
            return HardenedFillingsBlock.getCloneItemStack(level, pos, state);
        }

        @Override
        protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
            return HardenedFillingsBlock.getDrops(state, params);
        }

        @Override
        protected ItemInteractionResult useItemOn(
                ItemStack stack, BlockState state, Level level, BlockPos pos,
                Player player, InteractionHand hand, BlockHitResult hit
        ) {
            ItemInteractionResult axe = MooncakeAxeScrape.useAxeOnBlock(stack, state, level, pos, player, hand);
            if (axe.consumesAction()) {
                return axe;
            }
            return MooncakeWaterOxidation.useWaterBottleOnBlock(stack, state, level, pos, player, hand);
        }
    }

    public static class WeatheringDoor extends WeatheringCopperDoorBlock implements EntityBlock {
        public WeatheringDoor(BlockSetType type, WeatheringCopper.WeatherState weatherState, Properties properties) {
            super(type, weatherState, properties);
        }

        @Nullable
        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return HardenedFillingsBlock.newBlockEntity(pos, state);
        }

        @Override
        protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
            HardenedFillingsBlock.onRemove(state, level, pos, newState, moved);
        }

        @Override
        protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
            HardenedFillingsBlock.onPlace(state, level, pos, oldState, moved);
        }

        @Override
        public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
            super.setPlacedBy(level, pos, state, placer, stack);
            HardenedFillingsBlock.setPlacedBy(level, pos, state, placer, stack);
        }

        @Override
        public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
            return HardenedFillingsBlock.getCloneItemStack(level, pos, state);
        }

        @Override
        protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
            return HardenedFillingsBlock.getDrops(state, params);
        }

        @Override
        protected ItemInteractionResult useItemOn(
                ItemStack stack, BlockState state, Level level, BlockPos pos,
                Player player, InteractionHand hand, BlockHitResult hit
        ) {
            ItemInteractionResult axe = MooncakeAxeScrape.useAxeOnBlock(stack, state, level, pos, player, hand);
            if (axe.consumesAction()) {
                return axe;
            }
            return MooncakeWaterOxidation.useWaterBottleOnBlock(stack, state, level, pos, player, hand);
        }
    }

    public static class WaxedDoor extends DoorBlock implements EntityBlock {
        public WaxedDoor(BlockSetType type, Properties properties) {
            super(type, properties);
        }

        @Nullable
        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return HardenedFillingsBlock.newBlockEntity(pos, state);
        }

        @Override
        protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
            HardenedFillingsBlock.onRemove(state, level, pos, newState, moved);
        }

        @Override
        protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
            HardenedFillingsBlock.onPlace(state, level, pos, oldState, moved);
        }

        @Override
        public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
            super.setPlacedBy(level, pos, state, placer, stack);
            HardenedFillingsBlock.setPlacedBy(level, pos, state, placer, stack);
        }

        @Override
        public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
            return HardenedFillingsBlock.getCloneItemStack(level, pos, state);
        }

        @Override
        protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
            return HardenedFillingsBlock.getDrops(state, params);
        }

        @Override
        protected ItemInteractionResult useItemOn(
                ItemStack stack, BlockState state, Level level, BlockPos pos,
                Player player, InteractionHand hand, BlockHitResult hit
        ) {
            ItemInteractionResult axe = MooncakeAxeScrape.useAxeOnBlock(stack, state, level, pos, player, hand);
            if (axe.consumesAction()) {
                return axe;
            }
            return MooncakeWaterOxidation.useWaterBottleOnBlock(stack, state, level, pos, player, hand);
        }
    }
}
