package com.mooncake.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mooncake.component.MooncakeFillings.FillingGroup;
import com.mooncake.registry.ModComponents;
import com.mooncake.util.CrustHelper;
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

/**
 * Optional crust extras (月饼皮配料). Same grouping / merge rules as {@link MooncakeFillings}.
 * When non-empty, product names are wrapped as {@code {crust}皮 {rest}}.
 */
public record MooncakeCrust(List<FillingGroup> groups) implements TooltipProvider {
    public static final MooncakeCrust EMPTY = new MooncakeCrust(List.of());
    private static final int NAME_PREVIEW_COUNT = 2;

    public static final Codec<MooncakeCrust> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FillingGroup.CODEC.listOf().optionalFieldOf("groups", List.of()).forGetter(MooncakeCrust::groups)
    ).apply(instance, MooncakeCrust::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MooncakeCrust> STREAM_CODEC = StreamCodec.composite(
            FillingGroup.STREAM_CODEC.apply(ByteBufCodecs.list()),
            MooncakeCrust::groups,
            MooncakeCrust::new
    );

    public MooncakeCrust {
        List<FillingGroup> cleaned = new ArrayList<>();
        for (FillingGroup group : groups) {
            if (group != null && !group.isEmpty()) {
                cleaned.add(new FillingGroup(group.items(), group.mooncakeCount()));
            }
        }
        groups = List.copyOf(cleaned);
    }

    private MooncakeFillings asFillings() {
        return groups.isEmpty() ? MooncakeFillings.EMPTY : new MooncakeFillings(groups);
    }

    public static MooncakeCrust ofItems(List<ItemStack> stacks) {
        if (stacks == null || stacks.isEmpty()) {
            return EMPTY;
        }
        MooncakeFillings fillings = MooncakeFillings.ofItems(stacks);
        return fillings.isEmpty() ? EMPTY : new MooncakeCrust(fillings.groups());
    }

    public static MooncakeCrust of(List<ItemStack> stacks) {
        return ofItems(stacks);
    }

    public static MooncakeCrust combineFromSources(List<MooncakeCrust> sources) {
        List<MooncakeFillings> asFillings = new ArrayList<>();
        for (MooncakeCrust source : sources) {
            if (source != null && !source.isEmpty()) {
                asFillings.add(source.asFillings());
            }
        }
        MooncakeFillings combined = MooncakeFillings.combineFromSources(asFillings);
        return combined.isEmpty() ? EMPTY : new MooncakeCrust(combined.groups());
    }

    public static MooncakeCrust mixDistinctFromSources(List<MooncakeCrust> sources) {
        List<MooncakeFillings> asFillings = new ArrayList<>();
        for (MooncakeCrust source : sources) {
            if (source != null && !source.isEmpty()) {
                asFillings.add(source.asFillings());
            }
        }
        MooncakeFillings mixed = MooncakeFillings.mixDistinctFromSources(asFillings);
        return mixed.isEmpty() ? EMPTY : new MooncakeCrust(mixed.groups());
    }

    public List<ItemStack> items() {
        return asFillings().items();
    }

    public boolean isEmpty() {
        return groups.isEmpty();
    }

    public int totalCount() {
        return asFillings().totalCount();
    }

    /** {@code 金苹果、可可} — the {@code xx} in {@code xx皮}. */
    public Component label(boolean showFull) {
        int limit = showFull ? groups.size() : Math.min(groups.size(), NAME_PREVIEW_COUNT);
        MutableComponent joined = Component.empty();
        for (int i = 0; i < limit; i++) {
            if (i > 0) {
                joined.append(Component.translatable("item.mooncake.filling_group_separator"));
            }
            FillingGroup group = groups.get(i);
            MutableComponent groupLabel = Component.empty();
            for (int j = 0; j < group.items().size(); j++) {
                if (j > 0) {
                    groupLabel.append(Component.translatable("item.mooncake.filling_separator"));
                }
                groupLabel.append(CrustHelper.crustLabel(group.items().get(j)));
            }
            if (group.mooncakeCount() > 1) {
                joined.append(Component.translatable("item.mooncake.filling_counted", groupLabel, group.mooncakeCount()));
            } else {
                joined.append(groupLabel);
            }
        }
        if (!showFull && groups.size() > NAME_PREVIEW_COUNT) {
            joined.append(Component.literal("…"));
        }
        return joined;
    }

    /** Wrap a product name: {@code 金苹果皮 甜浆果月饼}. */
    public Component applyToName(Component productName) {
        if (isEmpty()) {
            return productName;
        }
        return Component.translatable(
                "item.mooncake.crust_wrap",
                label(MooncakeFillings.isShiftDownClient()),
                productName
        );
    }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag) {
        List<ItemStack> flat = items();
        if (flat.isEmpty()) {
            return;
        }
        boolean showFull = MooncakeFillings.isShiftDownClient() || flat.size() <= NAME_PREVIEW_COUNT;
        consumer.accept(Component.translatable("tooltip.mooncake.crust"));
        if (showFull) {
            for (ItemStack stack : flat) {
                consumer.accept(Component.literal("  ").append(CrustHelper.crustLabel(stack)));
            }
        } else {
            for (int i = 0; i < NAME_PREVIEW_COUNT; i++) {
                consumer.accept(Component.literal("  ").append(CrustHelper.crustLabel(flat.get(i))));
            }
            consumer.accept(Component.literal("  …").withStyle(ChatFormatting.DARK_GRAY));
            consumer.accept(Component.translatable("tooltip.mooncake.hold_shift_full").withStyle(ChatFormatting.GRAY));
        }
    }

    public static MooncakeCrust get(ItemStack stack) {
        return stack.getOrDefault(ModComponents.CRUST.get(), EMPTY);
    }

    public static void set(ItemStack stack, MooncakeCrust crust) {
        if (crust == null || crust.isEmpty()) {
            stack.remove(ModComponents.CRUST.get());
        } else {
            List<FillingGroup> copied = new ArrayList<>(crust.groups().size());
            for (FillingGroup group : crust.groups()) {
                copied.add(new FillingGroup(group.items(), group.mooncakeCount()));
            }
            stack.set(ModComponents.CRUST.get(), new MooncakeCrust(copied));
        }
    }
}
