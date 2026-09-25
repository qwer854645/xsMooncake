package com.mooncake.client;

import com.mooncake.MooncakeMod;
import com.mooncake.entity.HardenedMooncakeArrow;
import com.mooncake.item.HardenedWeatherItem;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.WeatheringCopper;

public class HardenedMooncakeArrowRenderer extends ArrowRenderer<HardenedMooncakeArrow> {
    public HardenedMooncakeArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(HardenedMooncakeArrow entity) {
        String spoil = spoilPrefix(entity);
        return ResourceLocation.fromNamespaceAndPath(
                MooncakeMod.MOD_ID, "textures/entity/" + spoil + "hardened_mooncake_arrow.png");
    }

    private static String spoilPrefix(HardenedMooncakeArrow entity) {
        ItemStack pickup = entity.getPickupItemStackOrigin();
        if (!pickup.isEmpty() && pickup.getItem() instanceof HardenedWeatherItem weatherItem) {
            WeatheringCopper.WeatherState state = weatherItem.weather().state();
            return switch (state) {
                case UNAFFECTED -> "";
                case EXPOSED -> "exposed_";
                case WEATHERED -> "weathered_";
                case OXIDIZED -> "oxidized_";
            };
        }
        return "";
    }
}
