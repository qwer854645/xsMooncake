package com.mooncake;

import com.mooncake.component.MooncakeCrust;
import com.mooncake.component.MooncakeFillings;
import com.mooncake.item.HardenedBuildingBlockItem;
import com.mooncake.item.HardenedWeatherItem;
import com.mooncake.util.MooncakeDataTransfer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Transfers fillings + crust onto hardened building blocks / gear when crafted or stonecut.
 * Multiple mooncake ingredients become「 加 」-separated groups.
 */
public final class HardenedFillingsCraftHandler {
    private HardenedFillingsCraftHandler() {
    }

    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();
        if (!acceptsFillings(result.getItem())) {
            return;
        }
        // Recipe may already have set data (e.g. block-from-cakes / gear shaped).
        if (!MooncakeFillings.get(result).isEmpty() || !MooncakeCrust.get(result).isEmpty()) {
            return;
        }

        List<ItemStack> sources = new ArrayList<>();
        var container = event.getInventory();
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack slot = container.getItem(i);
            if (!slot.isEmpty()) {
                sources.add(slot);
            }
        }
        applySources(result, sources);
    }

    /** Called from building-block items when taken from a stonecutter. */
    public static void copyFromStonecutter(ItemStack result, Level level, Player player) {
        if (level.isClientSide || !acceptsFillings(result.getItem())) {
            return;
        }
        if (!MooncakeFillings.get(result).isEmpty() || !MooncakeCrust.get(result).isEmpty()) {
            return;
        }
        if (!(player.containerMenu instanceof StonecutterMenu menu)) {
            return;
        }
        ItemStack input = menu.getSlot(0).getItem();
        if (input.isEmpty()) {
            return;
        }
        MooncakeDataTransfer.copyAll(input, result);
    }

    private static void applySources(ItemStack result, List<ItemStack> sources) {
        List<MooncakeFillings> fillingSources = new ArrayList<>();
        List<MooncakeCrust> crustSources = new ArrayList<>();
        for (ItemStack source : sources) {
            MooncakeFillings fillings = MooncakeFillings.get(source);
            if (!fillings.isEmpty()) {
                fillingSources.add(fillings);
            }
            MooncakeCrust crust = MooncakeCrust.get(source);
            if (!crust.isEmpty()) {
                crustSources.add(crust);
            }
        }
        applyFillings(result, fillingSources);
        applyCrust(result, crustSources);
    }

    private static void applyFillings(ItemStack result, List<MooncakeFillings> sources) {
        if (sources.isEmpty()) {
            return;
        }
        boolean buildingDerivative = result.getItem() instanceof HardenedBuildingBlockItem
                || result.getItem() instanceof HardenedBuildingBlockItem.Door;
        if (sources.size() == 1 || (buildingDerivative && allSameFillings(sources))) {
            MooncakeFillings.set(result, sources.getFirst());
            return;
        }
        MooncakeFillings.set(result, MooncakeFillings.combineFromSources(sources));
    }

    private static void applyCrust(ItemStack result, List<MooncakeCrust> sources) {
        if (sources.isEmpty()) {
            return;
        }
        boolean buildingDerivative = result.getItem() instanceof HardenedBuildingBlockItem
                || result.getItem() instanceof HardenedBuildingBlockItem.Door;
        if (sources.size() == 1 || (buildingDerivative && allSameCrust(sources))) {
            MooncakeCrust.set(result, sources.getFirst());
            return;
        }
        MooncakeCrust.set(result, MooncakeCrust.combineFromSources(sources));
    }

    private static boolean allSameFillings(List<MooncakeFillings> sources) {
        MooncakeFillings first = sources.getFirst();
        for (int i = 1; i < sources.size(); i++) {
            if (!first.equals(sources.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean allSameCrust(List<MooncakeCrust> sources) {
        MooncakeCrust first = sources.getFirst();
        for (int i = 1; i < sources.size(); i++) {
            if (!first.equals(sources.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean acceptsFillings(Item item) {
        return item instanceof HardenedBuildingBlockItem
                || item instanceof HardenedBuildingBlockItem.Door
                || item instanceof HardenedWeatherItem;
    }
}
