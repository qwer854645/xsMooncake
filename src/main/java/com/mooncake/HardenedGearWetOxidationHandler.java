package com.mooncake;

import com.mooncake.config.MooncakeConfig;
import com.mooncake.item.HardenedWeatherItem;
import com.mooncake.util.MooncakeItemVariants;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Unwaxed hardened gear / armor slowly oxidizes while worn or held
 * in water, rain, or humid biomes. Waxed items are immune.
 */
public final class HardenedGearWetOxidationHandler {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private HardenedGearWetOxidationHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) {
            return;
        }
        int interval = MooncakeConfig.equipmentWetOxidationInterval();
        if (interval <= 0 || player.tickCount % interval != 0) {
            return;
        }
        if (!isOxidizingEnvironment(player)) {
            return;
        }

        float chance = MooncakeConfig.equipmentWetOxidationChance();
        if (chance <= 0.0F) {
            return;
        }

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            tryOxidize(player, player.getItemBySlot(slot), slot, chance);
        }
        tryOxidize(player, player.getMainHandItem(), EquipmentSlot.MAINHAND, chance);
        tryOxidize(player, player.getOffhandItem(), EquipmentSlot.OFFHAND, chance);
    }

    private static void tryOxidize(Player player, ItemStack stack, EquipmentSlot slot, float chance) {
        if (stack.isEmpty() || !(stack.getItem() instanceof HardenedWeatherItem)) {
            return;
        }
        Optional<Item> next = MooncakeItemVariants.nextOxidation(stack.getItem());
        if (next.isEmpty()) {
            return;
        }
        if (player.getRandom().nextFloat() >= chance) {
            return;
        }
        ItemStack oxidized = MooncakeItemVariants.transmute(stack, next.get());
        player.setItemSlot(slot, oxidized);
        playOxidizeFeedback(player);
    }

    private static boolean isOxidizingEnvironment(Player player) {
        if (player.isInWaterOrRain() || player.isInWater()) {
            return true;
        }
        return isHumidBiome(player);
    }

    private static boolean isHumidBiome(Player player) {
        Holder<Biome> biome = player.level().getBiome(BlockPos.containing(player.position()));
        // Jungle / swamp-class climates: high downfall.
        return biome.value().getModifiedClimateSettings().downfall() >= 0.85F;
    }

    private static void playOxidizeFeedback(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.COPPER_HIT,
                SoundSource.PLAYERS,
                0.35F,
                0.8F + player.getRandom().nextFloat() * 0.2F
        );
        serverLevel.sendParticles(
                ParticleTypes.SPLASH,
                player.getX(),
                player.getY() + 1.0D,
                player.getZ(),
                6,
                0.3D,
                0.4D,
                0.3D,
                0.02D
        );
    }
}
