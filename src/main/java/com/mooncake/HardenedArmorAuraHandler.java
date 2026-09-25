package com.mooncake;

import com.mooncake.component.MooncakeFillings;
import com.mooncake.data.EatEffectsManager;
import com.mooncake.item.HardenedArmorItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Hardened mooncake armor declares wearers on equip; only those entities receive aura pulses.
 * Works for players and mobs (no world-wide living-entity scan).
 */
public final class HardenedArmorAuraHandler {
    private static final int POTION_INTERVAL = 40;
    private static final int ACTION_INTERVAL = 200;
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    /** Living entities currently wearing at least one hardened mooncake armor piece. */
    private static final Set<LivingEntity> WEARERS =
            Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

    private HardenedArmorAuraHandler() {
    }

    /** Called when hardened armor is equipped / unequipped, or when an entity joins with it on. */
    public static void declareWearer(LivingEntity living) {
        if (living == null || living.level().isClientSide) {
            return;
        }
        if (wearsHardenedArmor(living)) {
            WEARERS.add(living);
        } else {
            WEARERS.remove(living);
        }
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!isArmorSlot(event.getSlot())) {
            return;
        }
        if (!(isHardenedArmor(event.getFrom()) || isHardenedArmor(event.getTo()))) {
            return;
        }
        declareWearer(event.getEntity());
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        if (wearsHardenedArmor(living)) {
            WEARERS.add(living);
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (WEARERS.isEmpty()) {
            return;
        }
        Iterator<LivingEntity> iterator = WEARERS.iterator();
        while (iterator.hasNext()) {
            LivingEntity living = iterator.next();
            if (living == null || !living.isAlive() || living.isRemoved() || living.level().isClientSide) {
                iterator.remove();
                continue;
            }
            if (!wearsHardenedArmor(living)) {
                iterator.remove();
                continue;
            }

            int age = living.tickCount;
            boolean potionPulse = age % POTION_INTERVAL == 0;
            boolean actionPulse = age % ACTION_INTERVAL == 0;
            if (!potionPulse && !actionPulse) {
                continue;
            }

            MooncakeFillings fillings = collectArmorFillings(living);
            if (fillings.isEmpty()) {
                continue;
            }
            EatEffectsManager.INSTANCE.applyArmorAura(fillings, living, potionPulse, actionPulse);
        }
    }

    private static boolean wearsHardenedArmor(LivingEntity living) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            if (isHardenedArmor(living.getItemBySlot(slot))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isHardenedArmor(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof HardenedArmorItem;
    }

    private static boolean isArmorSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.HEAD
                || slot == EquipmentSlot.CHEST
                || slot == EquipmentSlot.LEGS
                || slot == EquipmentSlot.FEET;
    }

    private static MooncakeFillings collectArmorFillings(LivingEntity living) {
        List<MooncakeFillings> sources = new ArrayList<>();
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = living.getItemBySlot(slot);
            if (!isHardenedArmor(stack)) {
                continue;
            }
            MooncakeFillings fillings = MooncakeFillings.forEffects(stack);
            if (!fillings.isEmpty()) {
                sources.add(fillings);
            }
        }
        if (sources.isEmpty()) {
            return MooncakeFillings.EMPTY;
        }
        if (sources.size() == 1) {
            return sources.getFirst();
        }
        return MooncakeFillings.combineFromSources(sources);
    }
}
