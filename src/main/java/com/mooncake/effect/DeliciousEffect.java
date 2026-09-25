package com.mooncake.effect;

import com.mooncake.MooncakeMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** Speed + strength attributes with regeneration-like healing. */
public class DeliciousEffect extends MobEffect {
    public DeliciousEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF4A460);
        addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "effect.delicious_speed"),
                0.30D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "effect.delicious_strength"),
                5.0D,
                AttributeModifier.Operation.ADD_VALUE
        );
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (livingEntity.getHealth() < livingEntity.getMaxHealth()) {
            livingEntity.heal(1.5F + amplifier * 1.5F);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int interval = 40 >> amplifier;
        if (interval <= 0) {
            interval = 1;
        }
        return duration % interval == 0;
    }
}
