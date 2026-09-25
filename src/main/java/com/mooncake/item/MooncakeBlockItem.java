package com.mooncake.item;

import com.mooncake.block.MooncakeBlocks;
import com.mooncake.block.WaxedMooncakeBlock;
import com.mooncake.block.WeatheringMooncakeBlock;
import com.mooncake.blockentity.MooncakeBlockEntity;
import com.mooncake.component.MooncakeCrust;
import com.mooncake.component.MooncakeFillings;
import com.mooncake.config.MooncakeConfig;
import com.mooncake.entity.ThrownHardenedMooncake;
import com.mooncake.util.MooncakeEating;
import com.mooncake.util.MooncakeNames;
import com.mooncake.util.MooncakeNutrition;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class MooncakeBlockItem extends BlockItem implements ProjectileItem {
    private static final int BASE_NUTRITION = 4;
    private static final float BASE_SATURATION = 0.3F;
    private static final int MAX_NUTRITION = 20;
    private static final float MAX_SATURATION = 1.2F;

    public MooncakeBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    public boolean isHardenedCake() {
        Block block = this.getBlock();
        return (block instanceof WeatheringMooncakeBlock weathering && weathering.isHardened())
                || (block instanceof WaxedMooncakeBlock waxed && waxed.isHardened());
    }

    @Override
    public Component getName(ItemStack stack) {
        MooncakeFillings.stripRedundantWeather(stack);
        if (MooncakeBlocks.isPiece(this.getBlock())) {
            return MooncakeNames.forPiece(
                    this.getBlock(),
                    MooncakeFillings.get(stack),
                    MooncakeCrust.get(stack)
            );
        }
        return MooncakeFillings.get(stack).displayNameWithCrust(
                stack,
                com.mooncake.registry.ModBlocks.weatherOf(this.getBlock()),
                isHardenedCake()
        );
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            return super.useOn(context);
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (isHardenedCake()) {
            return throwHardened(level, player, hand);
        }
        return super.use(level, player, hand);
    }

    private InteractionResultHolder<ItemStack> throwHardened(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.SNOWBALL_THROW,
                SoundSource.NEUTRAL,
                0.6F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F)
        );
        if (!level.isClientSide) {
            ThrownHardenedMooncake thrown = new ThrownHardenedMooncake(level, player);
            thrown.setItem(stack.copyWithCount(1));
            thrown.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.35F, 1.0F);
            level.addFreshEntity(thrown);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        stack.consume(1, player);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        ThrownHardenedMooncake thrown = new ThrownHardenedMooncake(level, pos.x(), pos.y(), pos.z());
        thrown.setItem(stack.copyWithCount(1));
        return thrown;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        boolean placed = super.placeBlock(context, state);
        if (placed && context.getLevel().getBlockEntity(context.getClickedPos()) instanceof MooncakeBlockEntity be) {
            be.setFromStack(context.getItemInHand());
        }
        return placed;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        MooncakeFillings effectFillings = MooncakeFillings.forEffects(stack);
        ItemStack result = super.finishUsingItem(stack, level, livingEntity);
        if (!level.isClientSide) {
            // Foods are optional via config; potions / milk / ominous bottle always apply.
            MooncakeEating.applyIngredientConsumables(
                    effectFillings,
                    level,
                    livingEntity,
                    MooncakeConfig.applyFillingFoodsOnEat()
            );
            com.mooncake.data.EatEffectsManager.INSTANCE.applyFromFillings(effectFillings, livingEntity);
        }
        return result;
    }

    @Nullable
    @Override
    public FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        // Hardened cakes are throwable only — not edible.
        if (isHardenedCake()) {
            return null;
        }

        int baseNutrition = BASE_NUTRITION;
        float baseSaturation = BASE_SATURATION;

        if (MooncakeConfig.applyFillingFoodsOnEat()) {
            return new FoodProperties.Builder()
                    .nutrition(baseNutrition)
                    .saturationModifier(baseSaturation)
                    .alwaysEdible()
                    .build();
        }

        MooncakeNutrition.Bonus bonus = MooncakeNutrition.fromStack(stack);
        int nutrition = Math.min(MAX_NUTRITION, baseNutrition + bonus.nutrition());
        float saturation = Math.min(MAX_SATURATION, baseSaturation + bonus.saturation());

        return new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationModifier(saturation)
                .alwaysEdible()
                .build();
    }
}
