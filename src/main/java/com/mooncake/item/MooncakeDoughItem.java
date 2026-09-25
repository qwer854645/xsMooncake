package com.mooncake.item;

import com.mooncake.component.MooncakeCrust;
import com.mooncake.component.MooncakeFillings;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Mooncake dough (月饼皮); may carry optional crust extras from the mixing bowl. */
public class MooncakeDoughItem extends Item {
    public MooncakeDoughItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        MooncakeFillings.stripRedundantWeather(stack);
        MooncakeCrust crust = MooncakeCrust.get(stack);
        Component base = super.getName(stack);
        if (crust.isEmpty()) {
            return base;
        }
        return crust.applyToName(base);
    }
}
