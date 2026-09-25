package com.mooncake.registry;

import com.mooncake.MooncakeMod;
import com.mooncake.registry.ModHardenedBlocks;
import com.mooncake.registry.ModHardenedGear;
import com.mooncake.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MooncakeMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.mooncake"))
            .icon(() -> new ItemStack(ModItems.HARDENED_MOONCAKE.get()))
            .displayItems((params, output) -> {
                output.accept(ModItems.MIXING_BOWL.get());
                output.accept(ModItems.MOONCAKE_WORKBENCH.get());
                output.accept(ModItems.MOONCAKE_NAMING_STAND.get());
                output.accept(ModItems.INCOMPLETE_MOONCAKE_DOUGH.get());
                output.accept(ModItems.MOONCAKE_DOUGH.get());
                output.accept(ModItems.RAW_MOONCAKE.get());
                output.accept(ModItems.PLASTIC_WRAP.get());

                output.accept(ModItems.MOONCAKE.get());
                output.accept(ModItems.EXPOSED_MOONCAKE.get());
                output.accept(ModItems.WEATHERED_MOONCAKE.get());
                output.accept(ModItems.OXIDIZED_MOONCAKE.get());
                output.accept(ModItems.WAXED_MOONCAKE.get());
                output.accept(ModItems.WAXED_EXPOSED_MOONCAKE.get());
                output.accept(ModItems.WAXED_WEATHERED_MOONCAKE.get());
                output.accept(ModItems.WAXED_OXIDIZED_MOONCAKE.get());

                output.accept(ModItems.MOONCAKE_PIECE.get());
                output.accept(ModItems.EXPOSED_MOONCAKE_PIECE.get());
                output.accept(ModItems.WEATHERED_MOONCAKE_PIECE.get());
                output.accept(ModItems.OXIDIZED_MOONCAKE_PIECE.get());
                output.accept(ModItems.WAXED_MOONCAKE_PIECE.get());
                output.accept(ModItems.WAXED_EXPOSED_MOONCAKE_PIECE.get());
                output.accept(ModItems.WAXED_WEATHERED_MOONCAKE_PIECE.get());
                output.accept(ModItems.WAXED_OXIDIZED_MOONCAKE_PIECE.get());

                output.accept(ModItems.HARDENED_MOONCAKE.get());
                output.accept(ModItems.EXPOSED_HARDENED_MOONCAKE.get());
                output.accept(ModItems.WEATHERED_HARDENED_MOONCAKE.get());
                output.accept(ModItems.OXIDIZED_HARDENED_MOONCAKE.get());
                output.accept(ModItems.WAXED_HARDENED_MOONCAKE.get());
                output.accept(ModItems.WAXED_EXPOSED_HARDENED_MOONCAKE.get());
                output.accept(ModItems.WAXED_WEATHERED_HARDENED_MOONCAKE.get());
                output.accept(ModItems.WAXED_OXIDIZED_HARDENED_MOONCAKE.get());

                output.accept(ModItems.HARDENED_MOONCAKE_PIECE.get());
                output.accept(ModItems.EXPOSED_HARDENED_MOONCAKE_PIECE.get());
                output.accept(ModItems.WEATHERED_HARDENED_MOONCAKE_PIECE.get());
                output.accept(ModItems.OXIDIZED_HARDENED_MOONCAKE_PIECE.get());
                output.accept(ModItems.WAXED_HARDENED_MOONCAKE_PIECE.get());
                output.accept(ModItems.WAXED_EXPOSED_HARDENED_MOONCAKE_PIECE.get());
                output.accept(ModItems.WAXED_WEATHERED_HARDENED_MOONCAKE_PIECE.get());
                output.accept(ModItems.WAXED_OXIDIZED_HARDENED_MOONCAKE_PIECE.get());

                for (var item : ModHardenedBlocks.ALL_ITEMS) {
                    output.accept(item.get());
                }
                for (var item : ModHardenedGear.ALL) {
                    output.accept(item.get());
                }
            })
            .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
