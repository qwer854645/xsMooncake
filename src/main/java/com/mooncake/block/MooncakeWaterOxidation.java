package com.mooncake.block;

import com.mooncake.item.HardenedBuildingBlockItem;
import com.mooncake.item.MooncakeBlockItem;
import com.mooncake.registry.ModHardenedBlocks;
import com.mooncake.util.MooncakeDataTransfer;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

/**
 * Water-bottle oxidation for placed mooncake blocks and held placeable mooncake items.
 * Waxed variants are unaffected.
 */
public final class MooncakeWaterOxidation {
    private MooncakeWaterOxidation() {
    }

    public static void onUseItemOnBlock(UseItemOnBlockEvent event) {
        if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.BLOCK) {
            return;
        }
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!isOxidizablePlaceableBlock(state.getBlock())) {
            return;
        }
        ItemStack stack = player.getItemInHand(event.getHand());
        ItemInteractionResult result = useWaterBottleOnBlock(stack, state, level, pos, player, event.getHand());
        if (result.consumesAction()) {
            event.cancelWithResult(result);
        }
    }

    /**
     * Water bottle in one hand + placeable mooncake item in the other → oxidize one item.
     */
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        InteractionHand waterHand = event.getHand();
        ItemStack water = player.getItemInHand(waterHand);
        if (!isWaterBottle(water)) {
            return;
        }
        InteractionHand targetHand = waterHand == InteractionHand.MAIN_HAND
                ? InteractionHand.OFF_HAND
                : InteractionHand.MAIN_HAND;
        ItemStack target = player.getItemInHand(targetHand);
        if (!isOxidizablePlaceableItem(target)) {
            return;
        }

        InteractionResult result = useWaterBottleOnItem(water, target, player.level(), player, waterHand, targetHand);
        if (result.consumesAction()) {
            event.setCancellationResult(result);
            event.setCanceled(true);
        }
    }

    public static boolean isWaterBottle(ItemStack stack) {
        if (!stack.is(Items.POTION)) {
            return false;
        }
        PotionContents contents = stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        return contents.potion().filter(holder -> holder.is(Potions.WATER)).isPresent();
    }

    /** Cakes, pieces, and hardened building blocks. */
    public static boolean isOxidizablePlaceableBlock(Block block) {
        return MooncakeBlocks.isMooncake(block) || ModHardenedBlocks.isFamily(block);
    }

    public static boolean isOxidizablePlaceableItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof MooncakeBlockItem
                || stack.getItem() instanceof HardenedBuildingBlockItem
                || stack.getItem() instanceof HardenedBuildingBlockItem.Door) {
            return stack.getItem() instanceof BlockItem blockItem
                    && isOxidizablePlaceableBlock(blockItem.getBlock());
        }
        return false;
    }

    public static boolean isWaxed(Block block) {
        if (block instanceof WaxedMooncakeBlock) {
            return true;
        }
        if (ModHardenedBlocks.isFamily(block)) {
            return ModHardenedBlocks.weatherOf(block).waxed();
        }
        return false;
    }

    public static Optional<Block> nextOxidationStage(Block block) {
        return WeatheringCopper.getNext(block);
    }

    public static Optional<ItemStack> nextOxidationItem(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return Optional.empty();
        }
        return nextOxidationStage(blockItem.getBlock()).map(next -> {
            ItemStack out = new ItemStack(next.asItem(), 1);
            MooncakeDataTransfer.copyAll(stack, out);
            return out;
        });
    }

    public static ItemInteractionResult useWaterBottleOnBlock(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand
    ) {
        if (!isWaterBottle(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        Block block = state.getBlock();
        if (isWaxed(block)) {
            if (!level.isClientSide) {
                level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 0.5F, 0.6F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return nextOxidationStage(block).map(next -> {
            if (!level.isClientSide) {
                level.setBlock(pos, next.withPropertiesOf(state), Block.UPDATE_ALL);
                level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.6F, 1.0F);
                spawnSplash(level, pos);
                consumeWaterBottle(stack, player, hand);
            } else {
                spawnSplash(level, pos);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }).orElseGet(() -> {
            if (!level.isClientSide) {
                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 0.4F, 1.4F);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        });
    }

    public static InteractionResult useWaterBottleOnItem(
            ItemStack water,
            ItemStack target,
            Level level,
            Player player,
            InteractionHand waterHand,
            InteractionHand targetHand
    ) {
        if (!isWaterBottle(water) || !isOxidizablePlaceableItem(target)) {
            return InteractionResult.PASS;
        }
        if (!(target.getItem() instanceof BlockItem blockItem)) {
            return InteractionResult.PASS;
        }

        Block block = blockItem.getBlock();
        if (isWaxed(block)) {
            if (!level.isClientSide) {
                level.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.HONEYCOMB_WAX_ON,
                        SoundSource.PLAYERS,
                        0.5F,
                        0.6F
                );
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        Optional<ItemStack> next = nextOxidationItem(target);
        if (next.isEmpty()) {
            if (!level.isClientSide) {
                level.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        SoundEvents.BOTTLE_EMPTY,
                        SoundSource.PLAYERS,
                        0.4F,
                        1.4F
                );
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (!level.isClientSide) {
            consumeWaterBottle(water, player, waterHand);
            target.shrink(1);
            ItemStack oxidized = next.get();
            if (target.isEmpty()) {
                player.setItemInHand(targetHand, oxidized);
            } else if (!player.getInventory().add(oxidized)) {
                player.drop(oxidized, false);
            }
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.BOTTLE_EMPTY,
                    SoundSource.PLAYERS,
                    1.0F,
                    1.0F
            );
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    SoundEvents.GENERIC_SPLASH,
                    SoundSource.PLAYERS,
                    0.6F,
                    1.0F
            );
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static void consumeWaterBottle(ItemStack stack, Player player, InteractionHand hand) {
        if (player.getAbilities().instabuild) {
            return;
        }
        ItemStack glassBottle = new ItemStack(Items.GLASS_BOTTLE);
        stack.shrink(1);
        if (stack.isEmpty()) {
            player.setItemInHand(hand, glassBottle);
        } else if (!player.getInventory().add(glassBottle)) {
            player.drop(glassBottle, false);
        }
    }

    private static void spawnSplash(Level level, BlockPos pos) {
        Vec3 center = Vec3.atBottomCenterOf(pos).add(0.0D, 0.35D, 0.0D);
        for (int i = 0; i < 8; i++) {
            level.addParticle(
                    ParticleTypes.SPLASH,
                    center.x + (level.random.nextDouble() - 0.5D) * 0.6D,
                    center.y,
                    center.z + (level.random.nextDouble() - 0.5D) * 0.6D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }
}
