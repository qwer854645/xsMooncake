package com.mooncake.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public final class CrustRulesManager extends SimplePreparableReloadListener<Map<ResourceLocation, CrustRule>> {
    public static final CrustRulesManager INSTANCE = new CrustRulesManager();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DIRECTORY = "mooncake/crust_rules";

    private Map<ResourceLocation, CrustRule> rules = Map.of();

    private CrustRulesManager() {
    }

    public CrustRule get(ResourceLocation itemId) {
        return rules.get(itemId);
    }

    public boolean hasRule(ResourceLocation itemId) {
        return rules.containsKey(itemId);
    }

    @Override
    protected Map<ResourceLocation, CrustRule> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, CrustRule> loaded = new HashMap<>();
        resourceManager.listResources(DIRECTORY, path -> path.getPath().endsWith(".json")).forEach((id, resource) -> {
            try (Reader reader = resource.openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                CrustRule rule = CrustRule.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
                loaded.put(rule.ingredient(), rule);
            } catch (Exception e) {
                LOGGER.error("Failed to load crust rule {}", id, e);
            }
        });
        return loaded;
    }

    @Override
    protected void apply(Map<ResourceLocation, CrustRule> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.rules = Map.copyOf(object);
        LOGGER.info("Loaded {} mooncake crust rules", rules.size());
    }
}
