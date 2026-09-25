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

public final class FillingRulesManager extends SimplePreparableReloadListener<Map<ResourceLocation, FillingRule>> {
    public static final FillingRulesManager INSTANCE = new FillingRulesManager();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String DIRECTORY = "mooncake/filling_rules";

    private Map<ResourceLocation, FillingRule> rules = Map.of();

    private FillingRulesManager() {
    }

    public FillingRule get(ResourceLocation itemId) {
        return rules.get(itemId);
    }

    @Override
    protected Map<ResourceLocation, FillingRule> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<ResourceLocation, FillingRule> loaded = new HashMap<>();
        resourceManager.listResources(DIRECTORY, path -> path.getPath().endsWith(".json")).forEach((id, resource) -> {
            try (Reader reader = resource.openAsReader()) {
                JsonElement json = JsonParser.parseReader(reader);
                FillingRule rule = FillingRule.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow();
                loaded.put(rule.ingredient(), rule);
            } catch (Exception e) {
                LOGGER.error("Failed to load filling rule {}", id, e);
            }
        });
        return loaded;
    }

    @Override
    protected void apply(Map<ResourceLocation, FillingRule> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.rules = Map.copyOf(object);
        LOGGER.info("Loaded {} mooncake filling rules", rules.size());
    }
}
