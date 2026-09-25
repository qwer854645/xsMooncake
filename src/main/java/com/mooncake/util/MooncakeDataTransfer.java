package com.mooncake.util;

import com.mooncake.component.MooncakeCrust;
import com.mooncake.component.MooncakeFillings;
import com.mooncake.component.MooncakeInscription;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.ItemStack;

/** Copy / merge fillings + crust between stacks. */
public final class MooncakeDataTransfer {
    private MooncakeDataTransfer() {
    }

    /** Copy fillings and crust from {@code from} onto {@code to} when the target is missing them. */
    public static void copyMissing(ItemStack from, ItemStack to) {
        if (from == null || to == null || from.isEmpty() || to.isEmpty()) {
            return;
        }
        MooncakeFillings fillings = MooncakeFillings.get(from);
        if (!fillings.isEmpty() && MooncakeFillings.get(to).isEmpty()) {
            MooncakeFillings.set(to, fillings);
        }
        MooncakeCrust crust = MooncakeCrust.get(from);
        if (!crust.isEmpty() && MooncakeCrust.get(to).isEmpty()) {
            MooncakeCrust.set(to, crust);
        }
        MooncakeInscription inscription = MooncakeInscription.get(from);
        if (!inscription.isEmpty() && MooncakeInscription.get(to).isEmpty()) {
            MooncakeInscription.set(to, inscription);
        }
        MooncakeFillings.stripRedundantWeather(to);
    }

    public static void copyAll(ItemStack from, ItemStack to) {
        if (from == null || to == null || from.isEmpty() || to.isEmpty()) {
            return;
        }
        MooncakeFillings.set(to, MooncakeFillings.get(from));
        MooncakeCrust.set(to, MooncakeCrust.get(from));
        MooncakeInscription.set(to, MooncakeInscription.get(from));
        MooncakeFillings.stripRedundantWeather(to);
    }

    public static void mergeFromSources(List<ItemStack> sources, ItemStack result) {
        if (result == null || result.isEmpty() || sources == null || sources.isEmpty()) {
            return;
        }
        if (MooncakeFillings.get(result).isEmpty()) {
            List<MooncakeFillings> groups = new ArrayList<>();
            for (ItemStack source : sources) {
                MooncakeFillings fillings = MooncakeFillings.get(source);
                if (!fillings.isEmpty()) {
                    groups.add(fillings);
                }
            }
            if (groups.size() == 1) {
                MooncakeFillings.set(result, groups.getFirst());
            } else if (groups.size() > 1) {
                MooncakeFillings.set(result, MooncakeFillings.combineFromSources(groups));
            }
        }
        if (MooncakeCrust.get(result).isEmpty()) {
            List<MooncakeCrust> crusts = new ArrayList<>();
            for (ItemStack source : sources) {
                MooncakeCrust crust = MooncakeCrust.get(source);
                if (!crust.isEmpty()) {
                    crusts.add(crust);
                }
            }
            if (crusts.size() == 1) {
                MooncakeCrust.set(result, crusts.getFirst());
            } else if (crusts.size() > 1) {
                MooncakeCrust.set(result, MooncakeCrust.combineFromSources(crusts));
            }
        }
    }
}
