package com.mooncake.data;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

/** Runs datapack eat-effect actions (possibly delayed). */
public final class EatActionExecutor {
    private static final Logger LOGGER = LogUtils.getLogger();

    private EatActionExecutor() {
    }

    public static void schedule(LivingEntity entity, EatEffectRule.ActionSpec action) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (entity.getRandom().nextFloat() >= action.chance()) {
            return;
        }

        UUID entityId = entity.getUUID();
        Runnable task = () -> {
            Entity found = serverLevel.getEntity(entityId);
            if (!(found instanceof LivingEntity living) || !living.isAlive()) {
                return;
            }
            run(living, serverLevel, action);
        };

        if (action.delayTicks() <= 0) {
            task.run();
            return;
        }
        int when = serverLevel.getServer().getTickCount() + action.delayTicks();
        serverLevel.getServer().tell(new TickTask(when, task));
    }

    private static void run(LivingEntity entity, ServerLevel level, EatEffectRule.ActionSpec action) {
        switch (action.type()) {
            case "explode" -> explode(entity, level, action);
            case "lightning" -> lightning(entity, level, action);
            case "summon" -> summon(entity, level, action);
            default -> LOGGER.warn("Unknown eat action type: {}", action.type());
        }
    }

    private static void explode(LivingEntity entity, ServerLevel level, EatEffectRule.ActionSpec action) {
        Vec3 pos = entity.position();
        Level.ExplosionInteraction interaction = action.destroyBlocks()
                ? Level.ExplosionInteraction.TNT
                : Level.ExplosionInteraction.NONE;
        UUID selfId = entity.getUUID();
        boolean hurtSelf = action.hurtSelf();
        ExplosionDamageCalculator calculator = new ExplosionDamageCalculator() {
            @Override
            public boolean shouldDamageEntity(Explosion explosion, Entity target) {
                if (!hurtSelf && target.getUUID().equals(selfId)) {
                    return false;
                }
                return super.shouldDamageEntity(explosion, target);
            }
        };
        DamageSource source = level.damageSources().explosion(entity, entity);
        level.explode(
                hurtSelf ? null : entity,
                source,
                calculator,
                pos.x,
                pos.y,
                pos.z,
                action.power(),
                false,
                interaction
        );
    }

    private static void lightning(LivingEntity entity, ServerLevel level, EatEffectRule.ActionSpec action) {
        Vec3 base = entity.position();
        int count = Math.max(1, action.count());
        for (int i = 0; i < count; i++) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt == null) {
                continue;
            }
            double ox = (i == 0) ? 0.0 : (entity.getRandom().nextDouble() - 0.5) * 1.5;
            double oz = (i == 0) ? 0.0 : (entity.getRandom().nextDouble() - 0.5) * 1.5;
            bolt.moveTo(base.x + ox, base.y, base.z + oz);
            level.addFreshEntity(bolt);
        }
    }

    private static void summon(LivingEntity entity, ServerLevel level, EatEffectRule.ActionSpec action) {
        Optional<ResourceLocation> id = action.entity();
        if (id.isEmpty()) {
            return;
        }
        Optional<EntityType<?>> type = BuiltInRegistries.ENTITY_TYPE.getOptional(id.get());
        if (type.isEmpty()) {
            LOGGER.warn("Unknown summon entity id: {}", id.get());
            return;
        }
        BlockPos pos = BlockPos.containing(entity.position());
        type.get().spawn(level, pos, MobSpawnType.TRIGGERED);
    }
}
