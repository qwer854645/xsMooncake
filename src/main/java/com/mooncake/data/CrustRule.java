package com.mooncake.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

/** Datapack rule for a crust (月饼皮) ingredient — same fields as {@link FillingRule}. */
public record CrustRule(
        ResourceLocation ingredient,
        int nutritionBonus,
        float saturationBonus,
        Optional<String> nameKey
) {
    public static final Codec<CrustRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("ingredient").forGetter(CrustRule::ingredient),
            Codec.INT.optionalFieldOf("nutrition_bonus", 1).forGetter(CrustRule::nutritionBonus),
            Codec.FLOAT.optionalFieldOf("saturation_bonus", 0.1F).forGetter(CrustRule::saturationBonus),
            Codec.STRING.optionalFieldOf("name_key").forGetter(CrustRule::nameKey)
    ).apply(instance, CrustRule::new));
}
