package com.mooncake;

import com.mooncake.block.MooncakeWaterOxidation;
import com.mooncake.config.MooncakeConfig;
import com.mooncake.data.CrustRulesManager;
import com.mooncake.data.EatEffectsManager;
import com.mooncake.data.FillingRulesManager;
import com.mooncake.registry.ModArmorMaterials;
import com.mooncake.registry.ModBlockEntities;
import com.mooncake.registry.ModBlocks;
import com.mooncake.registry.ModComponents;
import com.mooncake.registry.ModCreativeTabs;
import com.mooncake.registry.ModEffects;
import com.mooncake.registry.ModEntities;
import com.mooncake.registry.ModHardenedGear;
import com.mooncake.registry.ModItems;
import com.mooncake.registry.ModRecipes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@Mod(MooncakeMod.MOD_ID)
public class MooncakeMod {
    public static final String MOD_ID = "mooncake";

    public MooncakeMod(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, MooncakeConfig.SPEC);

        ModComponents.register(modBus);
        ModEffects.register(modBus);
        ModArmorMaterials.register(modBus);
        ModBlocks.register(modBus);
        ModItems.register(modBus);
        ModHardenedGear.register(modBus);
        ModEntities.register(modBus);
        ModBlockEntities.register(modBus);
        ModRecipes.register(modBus);
        ModCreativeTabs.register(modBus);

        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(MooncakeWaterOxidation::onUseItemOnBlock);
        NeoForge.EVENT_BUS.addListener(MooncakeWaterOxidation::onRightClickItem);
        NeoForge.EVENT_BUS.addListener(HardenedFillingsCraftHandler::onItemCrafted);
        NeoForge.EVENT_BUS.addListener(HardenedArmorAuraHandler::onEquipmentChange);
        NeoForge.EVENT_BUS.addListener(HardenedArmorAuraHandler::onEntityJoin);
        NeoForge.EVENT_BUS.addListener(HardenedArmorAuraHandler::onServerTick);
        NeoForge.EVENT_BUS.addListener(HardenedGearEffectHandler::onLivingDamage);
        NeoForge.EVENT_BUS.addListener(HardenedGearEffectHandler::onShieldBlock);
        NeoForge.EVENT_BUS.addListener(HardenedGearEffectHandler::onBlockBreak);
        NeoForge.EVENT_BUS.addListener(HardenedGearWetOxidationHandler::onPlayerTick);
    }

    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(FillingRulesManager.INSTANCE);
        event.addListener(CrustRulesManager.INSTANCE);
        event.addListener(EatEffectsManager.INSTANCE);
    }
}
