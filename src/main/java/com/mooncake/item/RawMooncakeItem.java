package com.mooncake.item;

import com.mooncake.component.MooncakeFillings;
import com.mooncake.component.MooncakeWeather;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class RawMooncakeItem extends Item {
    public RawMooncakeItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        MooncakeFillings.stripRedundantWeather(stack);
        Component filled = MooncakeFillings.get(stack).displayNameWithCrust(stack, MooncakeWeather.DEFAULT, false);
        return Component.translatable("item.mooncake.raw_wrap", filled);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("tooltip.mooncake.raw_hint").withStyle(ChatFormatting.GRAY));
    }
}
