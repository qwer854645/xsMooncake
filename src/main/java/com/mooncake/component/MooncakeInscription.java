package com.mooncake.component;

import com.mojang.serialization.Codec;
import com.mooncake.registry.ModComponents;
import java.util.Objects;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

/**
 * Mold inscription on a mooncake (max 8 chars). Does not change display name.
 */
public final class MooncakeInscription {
    public static final int MAX_CHARS = 8;
    public static final MooncakeInscription EMPTY = new MooncakeInscription("");

    public static final Codec<MooncakeInscription> CODEC =
            Codec.STRING.xmap(MooncakeInscription::of, MooncakeInscription::text);

    public static final StreamCodec<RegistryFriendlyByteBuf, MooncakeInscription> STREAM_CODEC =
            StreamCodec.composite(ByteBufCodecs.STRING_UTF8, MooncakeInscription::text, MooncakeInscription::of);

    private final String text;

    private MooncakeInscription(String text) {
        this.text = text == null ? "" : text;
    }

    public static MooncakeInscription of(String raw) {
        String cleaned = sanitize(raw);
        return cleaned.isEmpty() ? EMPTY : new MooncakeInscription(cleaned);
    }

    public String text() {
        return text;
    }

    public boolean isEmpty() {
        return text.isEmpty();
    }

    public static String sanitize(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder();
        raw.codePoints().forEach(cp -> {
            if (out.codePointCount(0, out.length()) >= MAX_CHARS) {
                return;
            }
            if (Character.isWhitespace(cp)) {
                return;
            }
            // Ideographs + letters/digits (allows mixed labels)
            if (Character.isIdeographic(cp)
                    || Character.isLetterOrDigit(cp)
                    || cp == '·'
                    || cp == '•') {
                out.appendCodePoint(cp);
            }
        });
        return out.toString();
    }

    public static MooncakeInscription get(ItemStack stack) {
        return stack.getOrDefault(ModComponents.INSCRIPTION.get(), EMPTY);
    }

    public static void set(ItemStack stack, MooncakeInscription inscription) {
        if (inscription == null || inscription.isEmpty()) {
            stack.remove(ModComponents.INSCRIPTION.get());
        } else {
            stack.set(ModComponents.INSCRIPTION.get(), inscription);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MooncakeInscription that)) {
            return false;
        }
        return Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }
}
