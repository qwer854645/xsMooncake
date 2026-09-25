package com.mooncake.item;

import com.mooncake.util.MooncakeItemVariants;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

/** Consumable wrap that seals mooncake-family blocks (replaces honeycomb waxing). */
public class PlasticWrapItem extends Item {
    public PlasticWrapItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Optional<BlockState> waxed = getWaxed(state);
        if (waxed.isEmpty()) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, stack);
        }

        level.setBlock(pos, waxed.get(), Block.UPDATE_ALL | Block.UPDATE_CLIENTS);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, waxed.get()));
        level.levelEvent(player, LevelEvent.PARTICLES_AND_SOUND_WAX_ON, pos, 0);
        spawnWrapParticles(level, pos);

        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static Optional<BlockState> getWaxed(BlockState state) {
        return MooncakeItemVariants.waxedBlock(state.getBlock())
                .map(block -> block.withPropertiesOf(state));
    }

    private static void spawnWrapParticles(LevelAccessor level, BlockPos pos) {
        // Match honeycomb wax-on feel with a light wrap shimmer.
        for (int i = 0; i < 6; i++) {
            level.addParticle(
                    ParticleTypes.WAX_ON,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.6D,
                    pos.getZ() + 0.5D,
                    (level.getRandom().nextDouble() - 0.5D) * 0.4D,
                    level.getRandom().nextDouble() * 0.2D,
                    (level.getRandom().nextDouble() - 0.5D) * 0.4D
            );
        }
        if (level instanceof Level realLevel) {
            realLevel.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.2F);
        }
    }
}
