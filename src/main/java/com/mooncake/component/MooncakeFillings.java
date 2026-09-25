package com.mooncake.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mooncake.client.ClientShiftAccess;
import com.mooncake.registry.ModComponents;
import com.mooncake.util.FillingHelper;
import com.mooncake.util.MooncakeNames;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.block.WeatheringCopper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

/**
 * Fillings stored as groups. Within a group item names join with {@code 、};
 * between groups (from different mooncake recipes) join with {@code  加 }.
 * Hardened products name each group as {@code 馅料月饼} / {@code 馅料月饼×n}.
 */
public record MooncakeFillings(List<FillingGroup> groups) implements TooltipProvider {
    public static final MooncakeFillings EMPTY = new MooncakeFillings(List.of());
    private static final int NAME_PREVIEW_COUNT = 2;

    public record FillingGroup(List<ItemStack> items, int mooncakeCount) {
        public static final Codec<FillingGroup> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ItemStack.CODEC.listOf().fieldOf("items").forGetter(FillingGroup::items),
                Codec.INT.optionalFieldOf("mooncake_count", 1).forGetter(FillingGroup::mooncakeCount)
        ).apply(instance, FillingGroup::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, FillingGroup> STREAM_CODEC = StreamCodec.composite(
                ItemStack.STREAM_CODEC.apply(ByteBufCodecs.list()),
                FillingGroup::items,
                ByteBufCodecs.VAR_INT,
                FillingGroup::mooncakeCount,
                FillingGroup::new
        );

        public FillingGroup(List<ItemStack> items) {
            this(items, 1);
        }

        public FillingGroup {
            items = List.copyOf(mergeStacks(canonicalizeStacks(items)));
            if (mooncakeCount < 1) {
                mooncakeCount = 1;
            }
        }

        public boolean isEmpty() {
            return items.isEmpty();
        }

        /**
         * ItemStack does not implement value equals; records would otherwise break stacking.
         * Compare composition as a multiset so item order does not matter.
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof FillingGroup other)) {
                return false;
            }
            return mooncakeCount == other.mooncakeCount && sameComposition(this, other);
        }

        @Override
        public int hashCode() {
            int hash = Integer.hashCode(mooncakeCount);
            // Order-independent: sum stack hashes (commutative).
            int itemsHash = 0;
            for (ItemStack stack : items) {
                int h = System.identityHashCode(stack.getItem());
                h = 31 * h + stack.getCount();
                h = 31 * h + stack.getComponents().hashCode();
                itemsHash += h;
            }
            return 31 * hash + itemsHash;
        }

        /** Item labels only, e.g. {@code 甜浆果×2、金苹果}. */
        public MutableComponent labelJoined() {
            MutableComponent joined = Component.empty();
            for (int i = 0; i < items.size(); i++) {
                if (i > 0) {
                    joined.append(Component.translatable("item.mooncake.filling_separator"));
                }
                joined.append(FillingHelper.fillingLabel(items.get(i)));
            }
            return joined;
        }

        /** Hardened-series unit: {@code 甜浆果月饼} / {@code 甜浆果月饼×4}. */
        public MutableComponent mooncakeUnitLabel() {
            Component unit = Component.translatable("item.mooncake.mooncake_unit", labelJoined());
            if (mooncakeCount > 1) {
                return Component.translatable("item.mooncake.filling_counted", unit, mooncakeCount);
            }
            return unit.copy();
        }
    }

    public static final Codec<MooncakeFillings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FillingGroup.CODEC.listOf().optionalFieldOf("groups", List.of()).forGetter(MooncakeFillings::groups),
            ItemStack.CODEC.listOf().optionalFieldOf("items", List.of()).forGetter(f -> List.of())
    ).apply(instance, (groups, legacyItems) -> {
        if (!groups.isEmpty()) {
            return new MooncakeFillings(groups);
        }
        if (!legacyItems.isEmpty()) {
            return ofItems(legacyItems);
        }
        return EMPTY;
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, MooncakeFillings> STREAM_CODEC = StreamCodec.composite(
            FillingGroup.STREAM_CODEC.apply(ByteBufCodecs.list()),
            MooncakeFillings::groups,
            MooncakeFillings::new
    );

    public MooncakeFillings {
        List<FillingGroup> cleaned = new ArrayList<>();
        for (FillingGroup group : groups) {
            if (group != null && !group.isEmpty()) {
                // Rematerialize so identical stacks always collapse to one entry + count.
                cleaned.add(new FillingGroup(group.items(), group.mooncakeCount()));
            }
        }
        groups = List.copyOf(cleaned);
    }

    public static List<ItemStack> mergeStacks(List<ItemStack> stacks) {
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            ItemStack copy = stack.copy();
            boolean found = false;
            for (ItemStack existing : merged) {
                if (ItemStack.isSameItemSameComponents(existing, copy)) {
                    // setCount — not grow — so component storage can exceed max stack size.
                    existing.setCount(existing.getCount() + copy.getCount());
                    found = true;
                    break;
                }
            }
            if (!found) {
                merged.add(copy);
            }
        }
        return merged;
    }

    /** Drop volatile / nested components so the same filling item compares equal across crafts. */
    private static List<ItemStack> canonicalizeStacks(List<ItemStack> stacks) {
        List<ItemStack> out = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }
            out.add(canonicalizeFilling(stack));
        }
        return out;
    }

    /**
     * Fillings are identified by item + count + a small set of meaningful components.
     * Stripping unrelated patch data (attribution, etc.) lets identical fillings merge when
     * combining cakes — otherwise 2×A+2×B becomes four groups and the name hides ×n.
     */
    private static ItemStack canonicalizeFilling(ItemStack stack) {
        ItemStack out = new ItemStack(stack.getItem(), stack.getCount());
        // Effect-bearing / identity components — keep so potions, stew, etc. still work after merge.
        copyIfPresent(stack, out, net.minecraft.core.component.DataComponents.POTION_CONTENTS);
        copyIfPresent(stack, out, net.minecraft.core.component.DataComponents.SUSPICIOUS_STEW_EFFECTS);
        copyIfPresent(stack, out, net.minecraft.core.component.DataComponents.OMINOUS_BOTTLE_AMPLIFIER);
        copyIfPresent(stack, out, net.minecraft.core.component.DataComponents.FOOD);
        copyIfPresent(stack, out, net.minecraft.core.component.DataComponents.CUSTOM_NAME);
        copyIfPresent(stack, out, net.minecraft.core.component.DataComponents.ENCHANTMENTS);
        copyIfPresent(stack, out, net.minecraft.core.component.DataComponents.STORED_ENCHANTMENTS);
        copyIfPresent(stack, out, net.minecraft.core.component.DataComponents.DYED_COLOR);
        copyIfPresent(stack, out, net.minecraft.core.component.DataComponents.PROFILE);
        return out;
    }

    private static <T> void copyIfPresent(
            ItemStack from,
            ItemStack to,
            net.minecraft.core.component.DataComponentType<T> type
    ) {
        T value = from.get(type);
        if (value != null) {
            to.set(type, value);
        }
    }

    /** Single-group fillings (one mooncake). Identical items collapse to one stack + count. */
    public static MooncakeFillings ofItems(List<ItemStack> stacks) {
        List<ItemStack> merged = mergeStacks(canonicalizeStacks(stacks));
        if (merged.isEmpty()) {
            return EMPTY;
        }
        return new MooncakeFillings(List.of(new FillingGroup(merged, 1)));
    }

    public static MooncakeFillings of(List<ItemStack> stacks) {
        return ofItems(stacks);
    }

    /**
     * Combine several mooncakes into one fillings set.
     * Identical per-cake fillings stack as {@code 馅料月饼×n}; different ones stay separate for「 加 」.
     */
    public static MooncakeFillings combineFromSources(List<MooncakeFillings> sources) {
        List<FillingGroup> groups = new ArrayList<>();
        for (MooncakeFillings source : sources) {
            if (source == null || source.isEmpty()) {
                continue;
            }
            for (FillingGroup incoming : source.groups()) {
                // Normalize before compare so pre/post-canonicalize groups still merge.
                FillingGroup normalized = new FillingGroup(incoming.items(), Math.max(1, incoming.mooncakeCount()));
                int match = indexOfSameComposition(groups, normalized);
                if (match >= 0) {
                    FillingGroup existing = groups.get(match);
                    groups.set(match, new FillingGroup(
                            existing.items(),
                            existing.mooncakeCount() + normalized.mooncakeCount()
                    ));
                } else {
                    groups.add(normalized);
                }
            }
        }
        return groups.isEmpty() ? EMPTY : new MooncakeFillings(groups);
    }

    /**
     * Reassemble pieces into one cake: each distinct filling composition appears once.
     * e.g. 2×A pieces + 2×B pieces → {@code A 加 B}, not {@code A×2 加 B×2}.
     */
    public static MooncakeFillings mixDistinctFromSources(List<MooncakeFillings> sources) {
        List<FillingGroup> groups = new ArrayList<>();
        for (MooncakeFillings source : sources) {
            if (source == null || source.isEmpty()) {
                continue;
            }
            for (FillingGroup incoming : source.groups()) {
                FillingGroup unit = new FillingGroup(incoming.items(), 1);
                if (indexOfSameComposition(groups, unit) < 0) {
                    groups.add(unit);
                }
            }
        }
        return groups.isEmpty() ? EMPTY : new MooncakeFillings(groups);
    }

    private static int indexOfSameComposition(List<FillingGroup> groups, FillingGroup candidate) {
        for (int i = 0; i < groups.size(); i++) {
            if (sameComposition(groups.get(i), candidate)) {
                return i;
            }
        }
        return -1;
    }

    /** Same fillings per mooncake unit (item + count + meaningful components; order-insensitive). */
    private static boolean sameComposition(FillingGroup a, FillingGroup b) {
        if (a.items().size() != b.items().size()) {
            return false;
        }
        List<ItemStack> remaining = new ArrayList<>();
        for (ItemStack stack : b.items()) {
            remaining.add(stack.copy());
        }
        for (ItemStack stackA : a.items()) {
            boolean found = false;
            for (int i = 0; i < remaining.size(); i++) {
                ItemStack stackB = remaining.get(i);
                if (stackA.getCount() == stackB.getCount() && ItemStack.isSameItemSameComponents(stackA, stackB)) {
                    remaining.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                return false;
            }
        }
        return remaining.isEmpty();
    }

    /** Flat item list for eating / matching / tooltips (mooncake counts applied). */
    public List<ItemStack> items() {
        List<ItemStack> all = new ArrayList<>();
        for (FillingGroup group : groups) {
            for (ItemStack stack : group.items()) {
                ItemStack copy = stack.copy();
                copy.setCount(Math.multiplyExact(stack.getCount(), group.mooncakeCount()));
                all.add(copy);
            }
        }
        return List.copyOf(mergeStacks(all));
    }

    public boolean isEmpty() {
        return groups.isEmpty();
    }

    public int totalCount() {
        int total = 0;
        for (FillingGroup group : groups) {
            int mooncakes = group.mooncakeCount();
            for (ItemStack stack : group.items()) {
                total += Math.multiplyExact(stack.getCount(), mooncakes);
            }
        }
        return total;
    }

    public static boolean isShiftDownClient() {
        return FMLEnvironment.dist == Dist.CLIENT && ClientShiftAccess.isShiftDown();
    }

    public Component displayName(boolean waxed) {
        return displayName(new MooncakeWeather(WeatheringCopper.WeatherState.UNAFFECTED, waxed), false);
    }

    public Component displayName(MooncakeWeather weather) {
        return displayName(weather, false);
    }

    public Component displayName(MooncakeWeather weather, boolean hardened) {
        return displayProduct(weather, hardened, isShiftDownClient(),
                hardened ? "item.mooncake.filled_hardened" : "item.mooncake.filled",
                hardened ? "item.mooncake.filled_hardened_truncated" : "item.mooncake.filled_truncated",
                hardened ? "item.mooncake.hardened_mooncake" : "item.mooncake.mooncake",
                false);
    }

    /** Prefer {@link MooncakeNames#forStackProduct} when crust may be present. */
    public Component displayNameWithCrust(ItemStack stack, MooncakeWeather weather, boolean hardened) {
        return MooncakeNames.forStackProduct(
                stack,
                weather,
                hardened ? "item.mooncake.filled_hardened" : "item.mooncake.filled",
                hardened ? "item.mooncake.filled_hardened_truncated" : "item.mooncake.filled_truncated",
                hardened ? "item.mooncake.hardened_mooncake" : "item.mooncake.mooncake",
                false
        );
    }

    public Component displayProduct(
            MooncakeWeather weather,
            boolean hardened,
            boolean showFull,
            String filledKey,
            String truncatedKey,
            String emptyKey
    ) {
        return displayProduct(weather, hardened, showFull, filledKey, truncatedKey, emptyKey, true);
    }

    public Component displayProduct(
            MooncakeWeather weather,
            boolean hardened,
            boolean showFull,
            String filledKey,
            String truncatedKey,
            String emptyKey,
            boolean mooncakeUnits
    ) {
        return MooncakeNames.withWeather(
                displayProductBase(showFull, filledKey, truncatedKey, emptyKey, mooncakeUnits),
                weather
        );
    }

    /** Product name without weather / crust wrapping. */
    public Component displayProductBase(
            boolean showFull,
            String filledKey,
            String truncatedKey,
            String emptyKey,
            boolean mooncakeUnits
    ) {
        if (isEmpty()) {
            return Component.translatable(emptyKey);
        }
        Component fillingsPart = fillingsLabel(showFull, mooncakeUnits);
        if (!showFull && groups.size() > NAME_PREVIEW_COUNT) {
            return Component.translatable(
                    truncatedKey,
                    groupLabel(groups.get(0), mooncakeUnits),
                    groupLabel(groups.get(1), mooncakeUnits)
            );
        }
        return Component.translatable(filledKey, fillingsPart);
    }

    private static Component groupLabel(FillingGroup group, boolean mooncakeUnits) {
        return mooncakeUnits ? group.mooncakeUnitLabel() : group.labelJoined();
    }

    /** {@code 甜浆果×2 加 金苹果} or hardened {@code 甜浆果月饼×2 加 金苹果月饼}. */
    public Component fillingsLabel(boolean showFull) {
        return fillingsLabel(showFull, false);
    }

    public Component fillingsLabel(boolean showFull, boolean mooncakeUnits) {
        int limit = showFull ? groups.size() : Math.min(groups.size(), NAME_PREVIEW_COUNT);
        MutableComponent joined = Component.empty();
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                joined.append(Component.translatable("item.mooncake.filling_group_separator"));
            }
            joined.append(groupLabel(groups.get(i), mooncakeUnits));
        }
        if (!showFull && groups.size() > NAME_PREVIEW_COUNT) {
            joined.append(Component.literal("…"));
        }
        return joined;
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag) {
        List<ItemStack> flat = items();
        if (flat.isEmpty()) {
            return;
        }

        boolean showFull = isShiftDownClient() || flat.size() <= NAME_PREVIEW_COUNT;
        consumer.accept(Component.translatable("tooltip.mooncake.fillings"));
        if (showFull) {
            for (ItemStack stack : flat) {
                consumer.accept(Component.literal("  ").append(FillingHelper.fillingLabel(stack)));
            }
        } else {
            for (int i = 0; i < NAME_PREVIEW_COUNT; i++) {
                consumer.accept(Component.literal("  ").append(FillingHelper.fillingLabel(flat.get(i))));
            }
            consumer.accept(Component.literal("  …").withStyle(ChatFormatting.DARK_GRAY));
            consumer.accept(Component.translatable("tooltip.mooncake.hold_shift_full")
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    public static MooncakeFillings get(ItemStack stack) {
        return stack.getOrDefault(ModComponents.FILLINGS.get(), EMPTY);
    }

    /**
     * Fillings + crust extras as one multiset for eat-effects / aura / gear procs.
     * Naming still treats crust separately ({@code xx皮}).
     */
    public static MooncakeFillings forEffects(ItemStack stack) {
        return withCrust(get(stack), MooncakeCrust.get(stack));
    }

    public static MooncakeFillings withCrust(MooncakeFillings fillings, MooncakeCrust crust) {
        MooncakeFillings base = fillings == null ? EMPTY : fillings;
        if (crust == null || crust.isEmpty()) {
            return base;
        }
        MooncakeFillings crustAs = new MooncakeFillings(crust.groups());
        if (base.isEmpty()) {
            return crustAs;
        }
        return combineFromSources(List.of(base, crustAs));
    }

    public static void set(ItemStack stack, MooncakeFillings fillings) {
        if (fillings.isEmpty()) {
            stack.remove(ModComponents.FILLINGS.get());
        } else {
            // Deep-copy groups so canonicalize/merge runs and sources never share instances.
            List<FillingGroup> copied = new ArrayList<>(fillings.groups().size());
            for (FillingGroup group : fillings.groups()) {
                copied.add(new FillingGroup(group.items(), group.mooncakeCount()));
            }
            stack.set(ModComponents.FILLINGS.get(), new MooncakeFillings(copied));
        }
        stripRedundantWeather(stack);
    }

    /**
     * Cakes / gear / building blocks already encode oxidation in the item id.
     * A leftover {@code weather} component makes identical stacks unstackable.
     */
    public static void stripRedundantWeather(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.has(ModComponents.WEATHER.get())) {
            return;
        }
        stack.remove(ModComponents.WEATHER.get());
    }
}
