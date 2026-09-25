package com.mooncake.entity;

import com.mooncake.registry.ModEntities;
import com.mooncake.registry.ModItems;
import com.mooncake.util.MooncakeEating;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class ThrownHardenedMooncake extends ThrowableItemProjectile {
    /** Comparable to a strong melee hit — hardened cake as a blunt projectile. */
    public static final float HIT_DAMAGE = 10.0F;

    public ThrownHardenedMooncake(EntityType<? extends ThrownHardenedMooncake> type, Level level) {
        super(type, level);
    }

    public ThrownHardenedMooncake(Level level, LivingEntity shooter) {
        super(ModEntities.THROWN_HARDENED_MOONCAKE.get(), shooter, level);
    }

    public ThrownHardenedMooncake(Level level, double x, double y, double z) {
        super(ModEntities.THROWN_HARDENED_MOONCAKE.get(), x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.HARDENED_MOONCAKE.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        Entity target = result.getEntity();
        target.hurt(this.damageSources().thrown(this, this.getOwner()), HIT_DAMAGE);
        if (!this.level().isClientSide && target instanceof LivingEntity living) {
            ItemStack stack = this.getItem().copy();
            if (!stack.isEmpty()) {
                MooncakeEating.applyAsEaten(stack, this.level(), living);
            }
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte) 3);
            this.discard();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 3) {
            ItemStack stack = this.getItem();
            for (int i = 0; i < 8; i++) {
                this.level().addParticle(
                        new ItemParticleOption(ParticleTypes.ITEM, stack),
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        ((double) this.random.nextFloat() - 0.5D) * 0.08D,
                        ((double) this.random.nextFloat() - 0.5D) * 0.08D,
                        ((double) this.random.nextFloat() - 0.5D) * 0.08D
                );
            }
        }
    }
}
