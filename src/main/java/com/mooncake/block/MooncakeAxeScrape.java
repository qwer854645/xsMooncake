package com.mooncake.block;

import com.mooncake.util.MooncakeItemVariants;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Axe scrape like copper: remove wrap first, otherwise reduce spoilage by one stage.
 */
public final class MooncakeAxeScrape {
    private MooncakeAxeScrape() {
    }

    public static boolean isAxe(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof AxeItem;
    }

    public static ItemInteractionResult useAxeOnBlock(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand
    ) {
        if (!isAxe(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        Optional<Block> next = MooncakeItemVariants.axeScrapeBlock(state.getBlock());
        if (next.isEmpty()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        BlockState newState = next.get().withPropertiesOf(state);
        boolean removingWrap = MooncakeItemVariants.unwaxedBlock(state.getBlock()).isPresent();

        if (!level.isClientSide) {
            level.setBlock(pos, newState, Block.UPDATE_ALL);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, newState));
            level.levelEvent(player, removingWrap ? LevelEvent.PARTICLES_WAX_OFF : LevelEvent.PARTICLES_SCRAPE, pos, 0);
            level.playSound(
                    null,
                    pos,
                    removingWrap ? SoundEvents.AXE_WAX_OFF : SoundEvents.AXE_SCRAPE,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
}
