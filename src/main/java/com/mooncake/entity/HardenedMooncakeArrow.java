package com.mooncake.entity;

import com.mooncake.HardenedGearEffectHandler;
import com.mooncake.component.MooncakeFillings;
import com.mooncake.registry.ModEntities;
import com.mooncake.registry.ModHardenedGear;
import javax.annotation.Nullable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Arrow fired from hardened mooncake ammo; applies fillings on hit. */
public class HardenedMooncakeArrow extends AbstractArrow {
    private MooncakeFillings fillings = MooncakeFillings.EMPTY;

    public HardenedMooncakeArrow(EntityType<? extends HardenedMooncakeArrow> type, Level level) {
        super(type, level);
    }

    public HardenedMooncakeArrow(Level level, LivingEntity shooter, ItemStack pickup, @Nullable ItemStack weapon) {
        super(ModEntities.HARDENED_MOONCAKE_ARROW.get(), shooter, level, pickup, weapon);
        this.fillings = MooncakeFillings.forEffects(pickup);
        // Bow / crossbow fillings/crust also ride along when present.
        if (weapon != null && !weapon.isEmpty()) {
            MooncakeFillings weaponFillings = MooncakeFillings.forEffects(weapon);
            if (!weaponFillings.isEmpty()) {
                if (this.fillings.isEmpty()) {
                    this.fillings = weaponFillings;
                } else {
                    this.fillings = MooncakeFillings.combineFromSources(java.util.List.of(this.fillings, weaponFillings));
                }
            }
        }
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);
        if (!this.level().isClientSide && !fillings.isEmpty()) {
            HardenedGearEffectHandler.procFillings(target, fillings, this.level().getRandom());
        }
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModHardenedGear.UNAFFECTED_ARROW.get());
    }
}
