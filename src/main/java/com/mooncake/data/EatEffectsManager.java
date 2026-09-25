package com.mooncake.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import com.mooncake.component.MooncakeFillings;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;

public final class EatEffectsManager extends SimplePreparableReloadListener<List<EatEffectRule>> {
    public static final EatEffectsManager INSTANCE = new EatEffectsManager();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DIRECTORY = "mooncake/eat_effects";

    private List<EatEffectRule> rules = List.of();

    private EatEffectsManager() {
    }

    public void applyFromFillings(MooncakeFillings fillings, LivingEntity entity) {
        if (fillings.isEmpty()) {
            return;
        }
        List<ItemStack> flat = fillings.items();
        for (EatEffectRule rule : rules) {
            if (!rule.matchesItems(flat)) {
                continue;
            }
            for (MobEffectInstance instance : rule.rollEffects(entity.getRandom())) {
                entity.addEffect(new MobEffectInstance(instance));
            }
            for (EatEffectRule.ActionSpec action : rule.actions()) {
                EatActionExecutor.schedule(entity, action);
            }
        }
    }

    /**
     * Weaker continuous aura from worn hardened armor.
     * Potion pulses keep effects topped up; action pulses are rarer and toned down.
     */
    public void applyArmorAura(
            MooncakeFillings fillings,
            LivingEntity entity,
            boolean applyPotions,
            boolean applyActions
    ) {
        if (fillings.isEmpty()) {
            return;
        }
        List<ItemStack> flat = fillings.items();
        for (EatEffectRule rule : rules) {
            if (!rule.matchesItems(flat)) {
                continue;
            }
            if (applyPotions) {
                for (MobEffectInstance instance : rule.rollArmorEffects(entity.getRandom())) {
                    entity.addEffect(instance);
                }
            }
            if (applyActions) {
                for (EatEffectRule.ActionSpec action : rule.actions()) {
                    EatActionExecutor.schedule(entity, action.forArmorAura());
                }
            }
        }
    }

    @Override
    protected List<EatEffectRule> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        List<EatEffectRule> loaded = new ArrayList<>();
        resourceManager.listResources(DIRECTORY, path -> path.getPath().endsWith(".json")).forEach((id, resource) -> {
            try (Reader reader = resource.openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                EatEffectRule rule = EatEffectRule.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
                loaded.add(rule);
            } catch (Exception e) {
                LOGGER.error("Failed to load eat effect rule {}", id, e);
            }
        });
        return loaded;
    }

    @Override
    protected void apply(List<EatEffectRule> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.rules = List.copyOf(object);
        LOGGER.info("Loaded {} mooncake eat effect rules", rules.size());
    }
}
