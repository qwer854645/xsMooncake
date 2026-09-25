package com.mooncake;

import com.mooncake.component.MooncakeFillings;
import com.mooncake.config.MooncakeConfig;
import com.mooncake.data.EatEffectsManager;
import com.mooncake.item.HardenedGearItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * Filled hardened weapons / tools can proc eat-effects (same as eating that mooncake)
 * at a configurable chance: weapons apply to the hit target, tools apply to the user on break.
 * Shields apply to the attacker when a block succeeds.
 */
public final class HardenedGearEffectHandler {
    private HardenedGearEffectHandler() {
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        LivingEntity target = event.getEntity();
        ItemStack weapon = event.getSource().getWeaponItem();
        if (weapon == null || weapon.isEmpty()) {
            if (event.getSource().getEntity() instanceof Player player) {
                weapon = player.getMainHandItem();
            } else {
                return;
            }
        }
        if (!isWeapon(weapon.getItem())) {
            return;
        }
        if (target == event.getSource().getEntity()) {
            return;
        }
        RandomSource random = target.getRandom();
        if (event.getSource().getEntity() instanceof Player player) {
            random = player.getRandom();
        }
        tryProc(target, weapon, MooncakeConfig.weaponFillingsEffectChance(), random);
    }

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        LivingEntity blocker = event.getEntity();
        if (blocker.level().isClientSide) {
            return;
        }
        ItemStack shield = blocker.getUseItem();
        if (shield.isEmpty() || !(shield.getItem() instanceof HardenedGearItems.Shield)) {
            // Off-hand / main-hand shield while blocking
            shield = blocker.getItemInHand(blocker.getUsedItemHand());
        }
        if (!(shield.getItem() instanceof HardenedGearItems.Shield)) {
            ItemStack main = blocker.getMainHandItem();
            ItemStack off = blocker.getOffhandItem();
            if (main.getItem() instanceof HardenedGearItems.Shield) {
                shield = main;
            } else if (off.getItem() instanceof HardenedGearItems.Shield) {
                shield = off;
            } else {
                return;
            }
        }
        if (!(event.getDamageSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }
        tryProc(attacker, shield, MooncakeConfig.weaponFillingsEffectChance(), blocker.getRandom());
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!isTool(stack.getItem())) {
            return;
        }
        tryProc(player, stack, MooncakeConfig.toolFillingsEffectChance(), player.getRandom());
    }

    /** Arrow / shared path: roll chance then apply aura-strength fillings effects. */
    public static void procFillings(LivingEntity beneficiary, MooncakeFillings fillings, RandomSource random) {
        float chance = MooncakeConfig.weaponFillingsEffectChance();
        if (chance <= 0.0F || fillings == null || fillings.isEmpty()) {
            return;
        }
        if (chance < 1.0F && random.nextFloat() >= chance) {
            return;
        }
        EatEffectsManager.INSTANCE.applyArmorAura(fillings, beneficiary, true, true);
    }

    private static void tryProc(
            LivingEntity beneficiary,
            ItemStack stack,
            float chance,
            RandomSource random
    ) {
        if (chance <= 0.0F) {
            return;
        }
        MooncakeFillings fillings = MooncakeFillings.forEffects(stack);
        if (fillings.isEmpty()) {
            return;
        }
        if (chance < 1.0F && random.nextFloat() >= chance) {
            return;
        }
        EatEffectsManager.INSTANCE.applyArmorAura(fillings, beneficiary, true, true);
    }

    private static boolean isWeapon(Item item) {
        return item instanceof HardenedGearItems.Sword
                || item instanceof HardenedGearItems.Axe
                || item instanceof HardenedGearItems.Trident
                || item instanceof HardenedGearItems.Mace
                || item instanceof HardenedGearItems.Bow
                || item instanceof HardenedGearItems.Crossbow;
    }

    private static boolean isTool(Item item) {
        return item instanceof HardenedGearItems.Pickaxe
                || item instanceof HardenedGearItems.Axe
                || item instanceof HardenedGearItems.Shovel
                || item instanceof HardenedGearItems.Hoe;
    }
}
