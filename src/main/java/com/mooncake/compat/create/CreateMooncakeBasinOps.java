package com.mooncake.compat.create;

import com.mooncake.component.MooncakeCrust;
import com.mooncake.component.MooncakeFillings;
import com.mooncake.config.MooncakeConfig;
import com.mooncake.registry.ModItems;
import com.mooncake.registry.ModTags;
import com.mooncake.util.CrustHelper;
import com.mooncake.util.FillingHelper;
import com.mooncake.util.MooncakeSeriesGuard;
import com.simibubi.create.content.kinetics.mixer.CompactingRecipe;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.item.SmartInventory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

/**
 * Create basin automation for mooncake dough finishing (mixer) and raw assembly (press/compacting).
 * Marker JSON recipes provide the Create recipe type; this logic attaches crust/fillings and
 * consumes optional extras that vanilla Create matching would leave behind.
 * <p>
 * Only reads/writes the basin <em>input</em> inventory — scanning the combined capability also
 * sees output slots, so a leftover product would incorrectly abort the next cycle.
 */
public final class CreateMooncakeBasinOps {
    private CreateMooncakeBasinOps() {
    }

    /**
     * @return {@code null} to keep Create's default apply; otherwise success/failure.
     */
    public static Boolean tryApply(BasinBlockEntity basin, Recipe<?> recipe, boolean test) {
        Level level = basin.getLevel();
        if (level == null) {
            return false;
        }
        if (isFinishDoughMixing(recipe, level)) {
            return applyFinishDough(basin, test);
        }
        if (isRawMooncakeCompacting(recipe, level)) {
            return applyRawMooncake(basin, test);
        }
        return null;
    }

    public static boolean isFinishDoughMixing(Recipe<?> recipe, Level level) {
        if (!(recipe instanceof MixingRecipe)) {
            return false;
        }
        ItemStack result = recipe.getResultItem(level.registryAccess());
        if (!result.is(ModItems.MOONCAKE_DOUGH.get())) {
            return false;
        }
        List<Ingredient> ingredients = recipe.getIngredients();
        if (ingredients.size() != 1) {
            return false;
        }
        return ingredients.getFirst().test(new ItemStack(ModItems.INCOMPLETE_MOONCAKE_DOUGH.get()));
    }

    public static boolean isRawMooncakeCompacting(Recipe<?> recipe, Level level) {
        if (!(recipe instanceof CompactingRecipe)) {
            return false;
        }
        ItemStack result = recipe.getResultItem(level.registryAccess());
        if (!result.is(ModItems.RAW_MOONCAKE.get())) {
            return false;
        }
        List<Ingredient> ingredients = recipe.getIngredients();
        if (ingredients.size() != 1) {
            return false;
        }
        return ingredients.getFirst().test(new ItemStack(ModItems.MOONCAKE_DOUGH.get()));
    }

    /**
     * Incomplete dough (+ optional valid crust extras only) → finished dough with crust data.
     * One incomplete per cycle; crust extras present in input ride along (up to config max).
     */
    private static boolean applyFinishDough(BasinBlockEntity basin, boolean test) {
        SmartInventory items = basin.getInputInventory();
        if (items == null) {
            return false;
        }

        int incompleteSlot = -1;
        List<SlotTake> crustTakes = new ArrayList<>();
        int crustCount = 0;

        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(ModItems.INCOMPLETE_MOONCAKE_DOUGH.get())) {
                if (incompleteSlot < 0) {
                    incompleteSlot = slot;
                }
                continue;
            }
            if (MooncakeSeriesGuard.isBlockedAsIngredient(stack) || !CrustHelper.isValidCrust(stack)) {
                return false;
            }
            int room = MooncakeConfig.maxCrusts() - crustCount;
            if (room <= 0) {
                continue;
            }
            int take = Math.min(stack.getCount(), room);
            if (take <= 0) {
                continue;
            }
            crustTakes.add(new SlotTake(slot, take, stack.copyWithCount(take)));
            crustCount += take;
        }

        if (incompleteSlot < 0) {
            return false;
        }

        ItemStack dough = new ItemStack(ModItems.MOONCAKE_DOUGH.get());
        if (!crustTakes.isEmpty()) {
            List<ItemStack> extras = new ArrayList<>();
            for (SlotTake take : crustTakes) {
                extras.add(take.sample());
            }
            MooncakeCrust.set(dough, MooncakeCrust.of(extras));
        }

        if (!accept(basin, List.of(dough), true)) {
            return false;
        }
        if (test) {
            return true;
        }

        items.extractItem(incompleteSlot, 1, false);
        for (SlotTake take : crustTakes) {
            items.extractItem(take.slot(), take.count(), false);
        }
        return accept(basin, List.of(dough), false);
    }

    /**
     * One dough + optional valid fillings → raw mooncake (press / compacting in basin).
     */
    private static boolean applyRawMooncake(BasinBlockEntity basin, boolean test) {
        SmartInventory items = basin.getInputInventory();
        if (items == null) {
            return false;
        }

        int doughSlot = -1;
        ItemStack doughStack = ItemStack.EMPTY;
        List<SlotTake> fillingTakes = new ArrayList<>();
        int fillingCount = 0;

        for (int slot = 0; slot < items.getSlots(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.is(ModTags.DOUGHS)) {
                if (doughSlot < 0) {
                    doughSlot = slot;
                    doughStack = stack;
                }
                continue;
            }
            if (MooncakeSeriesGuard.isBlockedAsIngredient(stack) || !FillingHelper.isValidFilling(stack)) {
                return false;
            }
            int room = MooncakeConfig.maxFillings() - fillingCount;
            if (room <= 0) {
                continue;
            }
            int take = Math.min(stack.getCount(), room);
            if (take <= 0) {
                continue;
            }
            fillingTakes.add(new SlotTake(slot, take, stack.copyWithCount(take)));
            fillingCount += take;
        }

        if (doughSlot < 0 || doughStack.isEmpty()) {
            return false;
        }

        ItemStack raw = new ItemStack(ModItems.RAW_MOONCAKE.get());
        MooncakeCrust.set(raw, MooncakeCrust.get(doughStack));
        if (!fillingTakes.isEmpty()) {
            List<ItemStack> fillings = new ArrayList<>();
            for (SlotTake take : fillingTakes) {
                fillings.add(take.sample());
            }
            MooncakeFillings.set(raw, MooncakeFillings.of(fillings));
        }

        if (!accept(basin, List.of(raw), true)) {
            return false;
        }
        if (test) {
            return true;
        }

        items.extractItem(doughSlot, 1, false);
        for (SlotTake take : fillingTakes) {
            items.extractItem(take.slot(), take.count(), false);
        }
        return accept(basin, List.of(raw), false);
    }

    private static boolean accept(BasinBlockEntity basin, List<ItemStack> results, boolean simulate) {
        return basin.acceptOutputs(results, Collections.emptyList(), simulate);
    }

    private record SlotTake(int slot, int count, ItemStack sample) {
    }
}
