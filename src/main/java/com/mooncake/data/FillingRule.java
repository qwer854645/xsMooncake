package com.mooncake.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public record FillingRule(
        ResourceLocation ingredient,
        int nutritionBonus,
        float saturationBonus,
        Optional<String> nameKey
) {
    public static final Codec<FillingRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("ingredient").forGetter(FillingRule::ingredient),
            Codec.INT.optionalFieldOf("nutrition_bonus", 1).forGetter(FillingRule::nutritionBonus),
            Codec.FLOAT.optionalFieldOf("saturation_bonus", 0.1F).forGetter(FillingRule::saturationBonus),
            Codec.STRING.optionalFieldOf("name_key").forGetter(FillingRule::nameKey)
    ).apply(instance, FillingRule::new));
}
