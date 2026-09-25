package com.mooncake.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class MooncakeConfig {
    public static final ModConfigSpec.IntValue MAX_FILLINGS_PER_MOONCAKE;
    public static final ModConfigSpec.IntValue MAX_CRUSTS_PER_MOONCAKE;
    public static final ModConfigSpec.BooleanValue BAN_MOONCAKE_SERIES_AS_INGREDIENTS;
    public static final ModConfigSpec.IntValue MIXING_BOWL_TIME;
    public static final ModConfigSpec.BooleanValue APPLY_FILLING_FOODS_ON_EAT;
    public static final ModConfigSpec.DoubleValue WEAPON_FILLINGS_EFFECT_CHANCE;
    public static final ModConfigSpec.DoubleValue TOOL_FILLINGS_EFFECT_CHANCE;
    public static final ModConfigSpec.IntValue EQUIPMENT_WET_OXIDATION_INTERVAL;
    public static final ModConfigSpec.DoubleValue EQUIPMENT_WET_OXIDATION_CHANCE;
    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("mooncake");
        MAX_FILLINGS_PER_MOONCAKE = builder
                .comment("Maximum number of filling items a single mooncake can hold.")
                .defineInRange("maxFillingsPerMooncake", 64, 1, 1024);
        APPLY_FILLING_FOODS_ON_EAT = builder
                .comment(
                        "When true, eating a mooncake also fully applies nutrition, saturation, status effects,",
                        "and other eat-effects of every food filling inside (as if each food was eaten)."
                )
                .define("applyFillingFoodsOnEat", true);
        WEAPON_FILLINGS_EFFECT_CHANCE = builder
                .comment(
                        "Chance (0.0–1.0) that hitting an entity with a filled hardened mooncake weapon",
                        "applies that weapon's mooncake eat-effects to the hit target."
                )
                .defineInRange("weaponFillingsEffectChance", 0.20D, 0.0D, 1.0D);
        TOOL_FILLINGS_EFFECT_CHANCE = builder
                .comment(
                        "Chance (0.0–1.0) that breaking a block with a filled hardened mooncake tool",
                        "applies that tool's mooncake eat-effects to the user."
                )
                .defineInRange("toolFillingsEffectChance", 0.15D, 0.0D, 1.0D);
        MAX_CRUSTS_PER_MOONCAKE = builder
                .comment("Maximum number of crust (月饼皮) extra items a single mooncake can hold.")
                .defineInRange("maxCrustsPerMooncake", 16, 0, 1024);
        BAN_MOONCAKE_SERIES_AS_INGREDIENTS = builder
                .comment(
                        "When true, items from this mod's mooncake series cannot be used as fillings or crust extras.",
                        "Trying to insert them shows a warning. Disable only if you accept possible save issues."
                )
                .define("banMooncakeSeriesAsIngredients", true);
        MIXING_BOWL_TIME = builder
                .comment("Ticks to mix mooncake dough in a mixing bowl. Default 100 = 5 seconds.")
                .defineInRange("mixingBowlTime", 100, 20, 12000);
        EQUIPMENT_WET_OXIDATION_INTERVAL = builder
                .comment(
                        "How often (in ticks) to roll wet oxidation for worn/held unwaxed hardened gear.",
                        "Set to 0 to disable. Default 100 = every 5 seconds."
                )
                .defineInRange("equipmentWetOxidationInterval", 100, 0, 1200);
        EQUIPMENT_WET_OXIDATION_CHANCE = builder
                .comment(
                        "Chance (0.0–1.0) per check that one unwaxed worn/held hardened gear piece",
                        "advances one oxidation stage while in water, rain, or a humid biome.",
                        "Waxed gear never oxidizes this way. Default 0.01 = 1%."
                )
                .defineInRange("equipmentWetOxidationChance", 0.01D, 0.0D, 1.0D);
        builder.pop();
        SPEC = builder.build();
    }

    private MooncakeConfig() {
    }

    public static int maxFillings() {
        return MAX_FILLINGS_PER_MOONCAKE.get();
    }

    public static int maxCrusts() {
        return MAX_CRUSTS_PER_MOONCAKE.get();
    }

    public static boolean banMooncakeSeriesAsIngredients() {
        return BAN_MOONCAKE_SERIES_AS_INGREDIENTS.get();
    }

    public static int mixingBowlTime() {
        return MIXING_BOWL_TIME.get();
    }

    public static boolean applyFillingFoodsOnEat() {
        return APPLY_FILLING_FOODS_ON_EAT.get();
    }

    public static float weaponFillingsEffectChance() {
        return WEAPON_FILLINGS_EFFECT_CHANCE.get().floatValue();
    }

    public static float toolFillingsEffectChance() {
        return TOOL_FILLINGS_EFFECT_CHANCE.get().floatValue();
    }

    public static int equipmentWetOxidationInterval() {
        return EQUIPMENT_WET_OXIDATION_INTERVAL.get();
    }

    public static float equipmentWetOxidationChance() {
        return EQUIPMENT_WET_OXIDATION_CHANCE.get().floatValue();
    }
}
