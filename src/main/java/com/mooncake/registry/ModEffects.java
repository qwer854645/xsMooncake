package com.mooncake.registry;

import com.mooncake.MooncakeMod;
import com.mooncake.effect.DeliciousEffect;
import com.mooncake.effect.ToothAcheEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, MooncakeMod.MOD_ID);

    public static final DeferredHolder<MobEffect, ToothAcheEffect> TOOTH_ACHE =
            EFFECTS.register("tooth_ache", ToothAcheEffect::new);

    public static final DeferredHolder<MobEffect, DeliciousEffect> DELICIOUS =
            EFFECTS.register("delicious", DeliciousEffect::new);

    private ModEffects() {
    }

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }

    public static Holder<MobEffect> toothAche() {
        return TOOTH_ACHE;
    }

    public static Holder<MobEffect> delicious() {
        return DELICIOUS;
    }
}
