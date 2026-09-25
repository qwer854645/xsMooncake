package com.mooncake.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mooncake.component.MooncakeFillings;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record EatEffectRule(FillingMatch match, List<EffectSpec> effects, List<ActionSpec> actions) {
    public static final Codec<EatEffectRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FillingMatch.CODEC.fieldOf("match").forGetter(EatEffectRule::match),
            EffectSpec.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(EatEffectRule::effects),
            ActionSpec.CODEC.listOf().optionalFieldOf("actions", List.of()).forGetter(EatEffectRule::actions)
    ).apply(instance, EatEffectRule::new));

    public boolean matches(MooncakeFillings fillings) {
        return match.test(fillings.items());
    }

    /** Prefer when matching many rules against the same fillings (avoids repeated {@code items()} copies). */
    public boolean matchesItems(List<ItemStack> flatFillings) {
        return match.test(flatFillings);
    }

    public List<MobEffectInstance> rollEffects(RandomSource random) {
        List<MobEffectInstance> list = new ArrayList<>();
        for (EffectSpec spec : effects) {
            if (random.nextFloat() < spec.chance()) {
                spec.createInstance().ifPresent(list::add);
            }
        }
        return list;
    }

    /** Weaker, short-lived instances for continuous armor aura refreshes. */
    public List<MobEffectInstance> rollArmorEffects(RandomSource random) {
        List<MobEffectInstance> list = new ArrayList<>();
        for (EffectSpec spec : effects) {
            float armorChance = Math.min(1.0F, spec.chance() * 0.75F);
            if (random.nextFloat() < armorChance) {
                spec.createArmorInstance().ifPresent(list::add);
            }
        }
        return list;
    }

    public record FillingMatch(String type, Optional<ResourceLocation> tag, Optional<ResourceLocation> item) {
        public static final Codec<FillingMatch> CODEC = RecordCodecBuilder.<FillingMatch>create(instance -> instance.group(
                Codec.STRING.fieldOf("type").forGetter(FillingMatch::type),
                ResourceLocation.CODEC.optionalFieldOf("tag").forGetter(FillingMatch::tag),
                ResourceLocation.CODEC.optionalFieldOf("item").forGetter(FillingMatch::item)
        ).apply(instance, FillingMatch::new)).validate(FillingMatch::validate);

        private static DataResult<FillingMatch> validate(FillingMatch match) {
            return switch (match.type) {
                case "tag" -> match.tag.isPresent()
                        ? DataResult.success(match)
                        : DataResult.error(() -> "match.type=tag requires \"tag\"");
                case "item" -> match.item.isPresent()
                        ? DataResult.success(match)
                        : DataResult.error(() -> "match.type=item requires \"item\"");
                case "food" -> DataResult.success(match);
                default -> DataResult.error(() -> "Unknown match type: " + match.type + " (expected tag, item, or food)");
            };
        }

        public boolean test(List<ItemStack> fillings) {
            return switch (type) {
                case "tag" -> {
                    TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tag.orElseThrow());
                    for (ItemStack stack : fillings) {
                        if (stack.is(tagKey)) {
                            yield true;
                        }
                    }
                    yield false;
                }
                case "item" -> {
                    ResourceLocation id = item.orElseThrow();
                    for (ItemStack stack : fillings) {
                        if (BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(id)) {
                            yield true;
                        }
                    }
                    yield false;
                }
                case "food" -> {
                    for (ItemStack stack : fillings) {
                        if (stack.has(DataComponents.FOOD)) {
                            yield true;
                        }
                    }
                    yield false;
                }
                default -> false;
            };
        }
    }

    public record EffectSpec(
            ResourceLocation effect,
            int duration,
            int amplifier,
            boolean ambient,
            boolean visible,
            boolean showIcon,
            float chance
    ) {
        public static final Codec<EffectSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("effect").forGetter(EffectSpec::effect),
                Codec.INT.optionalFieldOf("duration", 200).forGetter(EffectSpec::duration),
                Codec.INT.optionalFieldOf("amplifier", 0).forGetter(EffectSpec::amplifier),
                Codec.BOOL.optionalFieldOf("ambient", false).forGetter(EffectSpec::ambient),
                Codec.BOOL.optionalFieldOf("visible", true).forGetter(EffectSpec::visible),
                Codec.BOOL.optionalFieldOf("show_icon", true).forGetter(EffectSpec::showIcon),
                Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter(EffectSpec::chance)
        ).apply(instance, EffectSpec::new));

        public Optional<MobEffectInstance> createInstance() {
            return BuiltInRegistries.MOB_EFFECT.getHolder(effect).map(holder -> new MobEffectInstance(
                    holder,
                    duration,
                    amplifier,
                    ambient,
                    visible,
                    showIcon
            ));
        }

        /** One amplifier lower, short duration, ambient particles. */
        public Optional<MobEffectInstance> createArmorInstance() {
            int armorAmp = Math.max(0, amplifier - 1);
            int armorDuration = Math.max(60, duration / 5);
            return BuiltInRegistries.MOB_EFFECT.getHolder(effect).map(holder -> new MobEffectInstance(
                    holder,
                    armorDuration,
                    armorAmp,
                    true,
                    visible,
                    showIcon
            ));
        }
    }

    /**
     * Special delayed / non-potion outcomes.
     * Types: {@code explode}, {@code lightning}, {@code summon}.
     */
    public record ActionSpec(
            String type,
            int delayTicks,
            float chance,
            float power,
            boolean destroyBlocks,
            boolean hurtSelf,
            int count,
            Optional<ResourceLocation> entity
    ) {
        public static final Codec<ActionSpec> CODEC = RecordCodecBuilder.<ActionSpec>create(instance -> instance.group(
                Codec.STRING.fieldOf("type").forGetter(ActionSpec::type),
                Codec.INT.optionalFieldOf("delay_ticks", 0).forGetter(ActionSpec::delayTicks),
                Codec.FLOAT.optionalFieldOf("chance", 1.0F).forGetter(ActionSpec::chance),
                Codec.FLOAT.optionalFieldOf("power", 4.0F).forGetter(ActionSpec::power),
                Codec.BOOL.optionalFieldOf("destroy_blocks", false).forGetter(ActionSpec::destroyBlocks),
                Codec.BOOL.optionalFieldOf("hurt_self", false).forGetter(ActionSpec::hurtSelf),
                Codec.INT.optionalFieldOf("count", 1).forGetter(ActionSpec::count),
                ResourceLocation.CODEC.optionalFieldOf("entity").forGetter(ActionSpec::entity)
        ).apply(instance, ActionSpec::new)).validate(ActionSpec::validate);

        private static DataResult<ActionSpec> validate(ActionSpec action) {
            return switch (action.type) {
                case "explode", "lightning" -> DataResult.success(action);
                case "summon" -> action.entity.isPresent()
                        ? DataResult.success(action)
                        : DataResult.error(() -> "actions.type=summon requires \"entity\"");
                default -> DataResult.error(() -> "Unknown action type: " + action.type
                        + " (expected explode, lightning, or summon)");
            };
        }

        /** Tonally weaker copy for armor aura pulses. */
        public ActionSpec forArmorAura() {
            return new ActionSpec(
                    type,
                    Math.max(delayTicks, 20),
                    Math.min(1.0F, chance * 0.35F),
                    power * 0.55F,
                    false,
                    false,
                    Math.max(1, count - 1),
                    entity
            );
        }
    }
}
