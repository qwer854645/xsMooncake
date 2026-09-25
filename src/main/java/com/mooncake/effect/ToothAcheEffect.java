package com.mooncake.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** Instant-damage burst on apply, then poison-like periodic damage. */
public class ToothAcheEffect extends MobEffect {
    public ToothAcheEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B6914);
    }

    @Override
    public void onEffectStarted(LivingEntity livingEntity, int amplifier) {
        livingEntity.hurt(livingEntity.damageSources().magic(), 5.0F + amplifier * 3.0F);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        livingEntity.hurt(livingEntity.damageSources().magic(), 1.5F + amplifier * 1.5F);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int interval = 20 >> amplifier;
        if (interval <= 0) {
            interval = 1;
        }
        return duration % interval == 0;
    }
}
