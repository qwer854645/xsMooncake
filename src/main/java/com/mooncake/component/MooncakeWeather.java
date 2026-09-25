package com.mooncake.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mooncake.registry.ModBlocks;
import com.mooncake.registry.ModComponents;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;

import java.util.function.IntFunction;

public record MooncakeWeather(WeatheringCopper.WeatherState state, boolean waxed) {
    public static final MooncakeWeather DEFAULT = new MooncakeWeather(WeatheringCopper.WeatherState.UNAFFECTED, false);

    private static final IntFunction<WeatheringCopper.WeatherState> BY_ID =
            ByIdMap.continuous(WeatheringCopper.WeatherState::ordinal, WeatheringCopper.WeatherState.values(), ByIdMap.OutOfBoundsStrategy.ZERO);

    public static final Codec<MooncakeWeather> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StringRepresentable.fromEnum(WeatheringCopper.WeatherState::values)
                    .optionalFieldOf("state", WeatheringCopper.WeatherState.UNAFFECTED)
                    .forGetter(MooncakeWeather::state),
            Codec.BOOL.optionalFieldOf("waxed", false).forGetter(MooncakeWeather::waxed)
    ).apply(instance, MooncakeWeather::new));

    public static final StreamCodec<ByteBuf, MooncakeWeather> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.idMapper(BY_ID, WeatheringCopper.WeatherState::ordinal),
            MooncakeWeather::state,
            ByteBufCodecs.BOOL,
            MooncakeWeather::waxed,
            MooncakeWeather::new
    );

    public Block resolveBlock() {
        return ModBlocks.byWeather(state, waxed);
    }

    public static MooncakeWeather fromBlock(Block block) {
        return ModBlocks.weatherOf(block);
    }

    public static MooncakeWeather get(ItemStack stack) {
        return stack.getOrDefault(ModComponents.WEATHER.get(), DEFAULT);
    }

    public static void set(ItemStack stack, MooncakeWeather weather) {
        if (weather.equals(DEFAULT)) {
            stack.remove(ModComponents.WEATHER.get());
        } else {
            stack.set(ModComponents.WEATHER.get(), weather);
        }
    }
}
