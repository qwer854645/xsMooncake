package com.mooncake.client;

import com.mooncake.MooncakeMod;
import com.mooncake.registry.ModBlockEntities;
import com.mooncake.registry.ModEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = MooncakeMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class MooncakeClient {
    private MooncakeClient() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.THROWN_HARDENED_MOONCAKE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.HARDENED_MOONCAKE_ARROW.get(), HardenedMooncakeArrowRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MIXING_BOWL.get(), MixingBowlRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MOONCAKE_WORKBENCH.get(), MooncakeWorkbenchRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MOONCAKE_NAMING_STAND.get(), MooncakeNamingStandRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MOONCAKE.get(), MooncakeInscriptionRenderer::new);
    }
}
