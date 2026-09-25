package com.mooncake.registry;

import com.mooncake.MooncakeMod;
import com.mooncake.block.MixingBowlBlock;
import com.mooncake.block.MooncakeBlocks;
import com.mooncake.block.MooncakeNamingStandBlock;
import com.mooncake.block.MooncakeWorkbenchBlock;
import com.mooncake.block.WaxedMooncakeBlock;
import com.mooncake.block.WeatheringMooncakeBlock;
import com.mooncake.component.MooncakeWeather;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MooncakeMod.MOD_ID);

    public static final DeferredBlock<MixingBowlBlock> MIXING_BOWL = BLOCKS.register(
            "mixing_bowl",
            () -> new MixingBowlBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(0.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    public static final DeferredBlock<MooncakeWorkbenchBlock> MOONCAKE_WORKBENCH = BLOCKS.register(
            "mooncake_workbench",
            () -> new MooncakeWorkbenchBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    public static final DeferredBlock<MooncakeNamingStandBlock> MOONCAKE_NAMING_STAND = BLOCKS.register(
            "mooncake_naming_stand",
            () -> new MooncakeNamingStandBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
                    .noOcclusion())
    );

    public static final DeferredBlock<WeatheringMooncakeBlock> MOONCAKE = BLOCKS.register(
            "mooncake",
            () -> new WeatheringMooncakeBlock(WeatheringCopper.WeatherState.UNAFFECTED, cakeProps(MapColor.GOLD, true, false), false)
    );
    public static final DeferredBlock<WeatheringMooncakeBlock> EXPOSED_MOONCAKE = BLOCKS.register(
            "exposed_mooncake",
            () -> new WeatheringMooncakeBlock(WeatheringCopper.WeatherState.EXPOSED, cakeProps(MapColor.TERRACOTTA_LIGHT_GRAY, true, false), false)
    );
    public static final DeferredBlock<WeatheringMooncakeBlock> WEATHERED_MOONCAKE = BLOCKS.register(
            "weathered_mooncake",
            () -> new WeatheringMooncakeBlock(WeatheringCopper.WeatherState.WEATHERED, cakeProps(MapColor.WARPED_STEM, true, false), false)
    );
    public static final DeferredBlock<WeatheringMooncakeBlock> OXIDIZED_MOONCAKE = BLOCKS.register(
            "oxidized_mooncake",
            () -> new WeatheringMooncakeBlock(WeatheringCopper.WeatherState.OXIDIZED, cakeProps(MapColor.WARPED_NYLIUM, true, false), false)
    );

    public static final DeferredBlock<WaxedMooncakeBlock> WAXED_MOONCAKE = BLOCKS.register(
            "waxed_mooncake",
            () -> new WaxedMooncakeBlock(WeatheringCopper.WeatherState.UNAFFECTED, cakeProps(MapColor.GOLD, false, false), false)
    );
    public static final DeferredBlock<WaxedMooncakeBlock> WAXED_EXPOSED_MOONCAKE = BLOCKS.register(
            "waxed_exposed_mooncake",
            () -> new WaxedMooncakeBlock(WeatheringCopper.WeatherState.EXPOSED, cakeProps(MapColor.TERRACOTTA_LIGHT_GRAY, false, false), false)
    );
    public static final DeferredBlock<WaxedMooncakeBlock> WAXED_WEATHERED_MOONCAKE = BLOCKS.register(
            "waxed_weathered_mooncake",
            () -> new WaxedMooncakeBlock(WeatheringCopper.WeatherState.WEATHERED, cakeProps(MapColor.WARPED_STEM, false, false), false)
    );
    public static final DeferredBlock<WaxedMooncakeBlock> WAXED_OXIDIZED_MOONCAKE = BLOCKS.register(
            "waxed_oxidized_mooncake",
            () -> new WaxedMooncakeBlock(WeatheringCopper.WeatherState.OXIDIZED, cakeProps(MapColor.WARPED_NYLIUM, false, false), false)
    );

    public static final DeferredBlock<WeatheringMooncakeBlock> MOONCAKE_PIECE = BLOCKS.register(
            "mooncake_piece",
            () -> new WeatheringMooncakeBlock(WeatheringCopper.WeatherState.UNAFFECTED, cakeProps(MapColor.GOLD, true, false), false, true)
    );
    public static final DeferredBlock<WeatheringMooncakeBlock> EXPOSED_MOONCAKE_PIECE = BLOCKS.register(
            "exposed_mooncake_piece",
            () -> new WeatheringMooncakeBlock(WeatheringCopper.WeatherState.EXPOSED, cakeProps(MapColor.TERRACOTTA_LIGHT_GRAY, true, false), false, true)
    );
    public static final DeferredBlock<WeatheringMooncakeBlock> WEATHERED_MOONCAKE_PIECE = BLOCKS.register(
            "weathered_mooncake_piece",
            () -> new WeatheringMooncakeBlock(WeatheringCopper.WeatherState.WEATHERED, cakeProps(MapColor.WARPED_STEM, true, false), false, true)
    );
    public static final DeferredBlock<WeatheringMooncakeBlock> OXIDIZED_MOONCAKE_PIECE = BLOCKS.register(
            "oxidized_mooncake_piece",
            () -> new WeatheringMooncakeBlock(WeatheringCopper.WeatherState.OXIDIZED, cakeProps(MapColor.WARPED_NYLIUM, true, false), false, true)
    );

    public static final DeferredBlock<WaxedMooncakeBlock> WAXED_MOONCAKE_PIECE = BLOCKS.register(
            "waxed_mooncake_piece",
            () -> new WaxedMooncakeBlock(WeatheringCopper.WeatherState.UNAFFECTED, cakeProps(MapColor.GOLD, false, false), false, true)
    );
    public static final DeferredBlock<WaxedMooncakeBlock> WAXED_EXPOSED_MOONCAKE_PIECE = BLOCKS.register(
            "waxed_exposed_mooncake_piece",
            () -> new WaxedMooncakeBlock(WeatheringCopper.WeatherState.EXPOSED, cakeProps(MapColor.TERRACOTTA_LIGHT_GRAY, false, false), false, true)
    );
    public static final DeferredBlock<WaxedMooncakeBlock> WAXED_WEATHERED_MOONCAKE_PIECE = BLOCKS.register(
            "waxed_weathered_mooncake_piece",
            () -> new WaxedMooncakeBlock(WeatheringCopper.WeatherState.WEATHERED, cakeProps(MapColor.WARPED_STEM, false, false), false, true)
    );
    public static final DeferredBlock<WaxedMooncakeBlock> WAXED_OXIDIZED_MOONCAKE_PIECE = BLOCKS.register(
            "waxed_oxidized_mooncake_piece",
            () -> new WaxedMooncakeBlock(WeatheringCopper.WeatherState.OXIDIZED, cakeProps(MapColor.WARPED_NYLIUM, false, false), false, true)
    );

    public static final DeferredBlock<WeatheringMooncakeBlock> HARDENED_MOONCAKE = BLOCKS.register(
            "hardened_mooncake",
            () -> new WeatheringMooncakeBlock(WeatheringCopper.WeatherState.UNAFFECTED, cakeProps(MapColor.TERRACOTTA_ORANGE, true, true), true)
    );
    public static final DeferredBlock<WeatheringMooncakeBlock> EXPOSED_HARDENED_MOONCAKE = BLOCKS.register(
            "exposed_hardened_mooncake",
            () -> new WeatheringMooncakeBlock(WeatheringCopper.WeatherState.EXPOSED, cakeProps(MapColor.TERRACOTTA_LIGHT_GRAY, true, true), true)
    );
    public static final DeferredBlock<WeatheringMooncakeBlock> WEATHERED_HARDENED_MOONCAKE = BLOCKS.register(
            "weathered_hardened_mooncake",
            () -> new WeatheringMooncakeBlock(WeatheringCopper.WeatherState.WEATHERED, cakeProps(MapColor.WARPED_STEM, true, true), true)
    );
    public static final DeferredBlock<WeatheringMooncakeBlock> OXIDIZED_HARDENED_MOONCAKE = BLOCKS.register(
            "oxidized_hardened_mooncake",
            () -> new WeatheringMooncakeBlock(WeatheringCopper.WeatherState.OXIDIZED, cakeProps(MapColor.WARPED_NYLIUM, true, true), true)
    );

    public static final DeferredBlock<WaxedMooncakeBlock> WAXED_HARDENED_MOONCAKE = BLOCKS.register(
            "waxed_hardened_mooncake",
            () -> new WaxedMooncakeBlock(WeatheringCopper.WeatherState.UNAFFECTED, cakeProps(MapColor.TERRACOTTA_ORANGE, false, true), true)
    );
    public static final DeferredBlock<WaxedMooncakeBlock> WAXED_EXPOSED_HARDENED_MOONCAKE = BLOCKS.register(
            "waxed_exposed_hardened_mooncake",
            () -> new WaxedMooncakeBlock(WeatheringCopper.WeatherState.EXPOSED, cakeProps(MapColor.TERRACOTTA_LIGHT_GRAY, false, true), true)
    );
    public static final DeferredBlock<WaxedMooncakeBlock> WAXED_WEATHERED_HARDENED_MOONCAKE = BLOCKS.register(
            "waxed_weathered_hardened_mooncake",
            () -> new WaxedMooncakeBlock(WeatheringCopper.WeatherState.WEATHERED, cakeProps(MapColor.WARPED_STEM, false, true), true)
    );
    public static final DeferredBlock<WaxedMooncakeBlock> WAXED_OXIDIZED_HARDENED_MOONCAKE = BLOCKS.register(
            "waxed_oxidized_hardened_mooncake",
            () -> new WaxedMooncakeBlock(WeatheringCopper.WeatherState.OXIDIZED, cakeProps(MapColor.WARPED_NYLIUM, false, true), true)
    );

    public static final DeferredBlock<WeatheringMooncakeBlock> HARDENED_MOONCAKE_PIECE = BLOCKS.register(
            "hardened_mooncake_piece",
            () -> new WeatheringMooncakeBlock(WeatheringCopper.WeatherState.UNAFFECTED, cakeProps(MapColor.TERRACOTTA_ORANGE, true, true), true, true)
    );
    public static final DeferredBlock<WeatheringMooncakeBlock> EXPOSED_HARDENED_MOONCAKE_PIECE = BLOCKS.register(
            "exposed_hardened_mooncake_piece",
            () -> new WeatheringMooncakeBlock(WeatheringCopper.WeatherState.EXPOSED, cakeProps(MapColor.TERRACOTTA_LIGHT_GRAY, true, true), true, true)
    );
    public static final DeferredBlock<WeatheringMooncakeBlock> WEATHERED_HARDENED_MOONCAKE_PIECE = BLOCKS.register(
            "weathered_hardened_mooncake_piece",
            () -> new WeatheringMooncakeBlock(WeatheringCopper.WeatherState.WEATHERED, cakeProps(MapColor.WARPED_STEM, true, true), true, true)
    );
    public static final DeferredBlock<WeatheringMooncakeBlock> OXIDIZED_HARDENED_MOONCAKE_PIECE = BLOCKS.register(
            "oxidized_hardened_mooncake_piece",
            () -> new WeatheringMooncakeBlock(WeatheringCopper.WeatherState.OXIDIZED, cakeProps(MapColor.WARPED_NYLIUM, true, true), true, true)
    );

    public static final DeferredBlock<WaxedMooncakeBlock> WAXED_HARDENED_MOONCAKE_PIECE = BLOCKS.register(
            "waxed_hardened_mooncake_piece",
            () -> new WaxedMooncakeBlock(WeatheringCopper.WeatherState.UNAFFECTED, cakeProps(MapColor.TERRACOTTA_ORANGE, false, true), true, true)
    );
    public static final DeferredBlock<WaxedMooncakeBlock> WAXED_EXPOSED_HARDENED_MOONCAKE_PIECE = BLOCKS.register(
            "waxed_exposed_hardened_mooncake_piece",
            () -> new WaxedMooncakeBlock(WeatheringCopper.WeatherState.EXPOSED, cakeProps(MapColor.TERRACOTTA_LIGHT_GRAY, false, true), true, true)
    );
    public static final DeferredBlock<WaxedMooncakeBlock> WAXED_WEATHERED_HARDENED_MOONCAKE_PIECE = BLOCKS.register(
            "waxed_weathered_hardened_mooncake_piece",
            () -> new WaxedMooncakeBlock(WeatheringCopper.WeatherState.WEATHERED, cakeProps(MapColor.WARPED_STEM, false, true), true, true)
    );
    public static final DeferredBlock<WaxedMooncakeBlock> WAXED_OXIDIZED_HARDENED_MOONCAKE_PIECE = BLOCKS.register(
            "waxed_oxidized_hardened_mooncake_piece",
            () -> new WaxedMooncakeBlock(WeatheringCopper.WeatherState.OXIDIZED, cakeProps(MapColor.WARPED_NYLIUM, false, true), true, true)
    );

    private ModBlocks() {
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ModHardenedBlocks.register(bus);
    }

    public static Block byWeather(WeatheringCopper.WeatherState state, boolean waxed) {
        return byWeather(state, waxed, false, false);
    }

    public static Block byWeather(WeatheringCopper.WeatherState state, boolean waxed, boolean hardened) {
        return byWeather(state, waxed, hardened, false);
    }

    public static Block byWeather(WeatheringCopper.WeatherState state, boolean waxed, boolean hardened, boolean piece) {
        if (piece) {
            if (hardened) {
                return switch (state) {
                    case UNAFFECTED -> waxed ? WAXED_HARDENED_MOONCAKE_PIECE.get() : HARDENED_MOONCAKE_PIECE.get();
                    case EXPOSED -> waxed ? WAXED_EXPOSED_HARDENED_MOONCAKE_PIECE.get() : EXPOSED_HARDENED_MOONCAKE_PIECE.get();
                    case WEATHERED -> waxed ? WAXED_WEATHERED_HARDENED_MOONCAKE_PIECE.get() : WEATHERED_HARDENED_MOONCAKE_PIECE.get();
                    case OXIDIZED -> waxed ? WAXED_OXIDIZED_HARDENED_MOONCAKE_PIECE.get() : OXIDIZED_HARDENED_MOONCAKE_PIECE.get();
                };
            }
            return switch (state) {
                case UNAFFECTED -> waxed ? WAXED_MOONCAKE_PIECE.get() : MOONCAKE_PIECE.get();
                case EXPOSED -> waxed ? WAXED_EXPOSED_MOONCAKE_PIECE.get() : EXPOSED_MOONCAKE_PIECE.get();
                case WEATHERED -> waxed ? WAXED_WEATHERED_MOONCAKE_PIECE.get() : WEATHERED_MOONCAKE_PIECE.get();
                case OXIDIZED -> waxed ? WAXED_OXIDIZED_MOONCAKE_PIECE.get() : OXIDIZED_MOONCAKE_PIECE.get();
            };
        }
        if (hardened) {
            return switch (state) {
                case UNAFFECTED -> waxed ? WAXED_HARDENED_MOONCAKE.get() : HARDENED_MOONCAKE.get();
                case EXPOSED -> waxed ? WAXED_EXPOSED_HARDENED_MOONCAKE.get() : EXPOSED_HARDENED_MOONCAKE.get();
                case WEATHERED -> waxed ? WAXED_WEATHERED_HARDENED_MOONCAKE.get() : WEATHERED_HARDENED_MOONCAKE.get();
                case OXIDIZED -> waxed ? WAXED_OXIDIZED_HARDENED_MOONCAKE.get() : OXIDIZED_HARDENED_MOONCAKE.get();
            };
        }
        return switch (state) {
            case UNAFFECTED -> waxed ? WAXED_MOONCAKE.get() : MOONCAKE.get();
            case EXPOSED -> waxed ? WAXED_EXPOSED_MOONCAKE.get() : EXPOSED_MOONCAKE.get();
            case WEATHERED -> waxed ? WAXED_WEATHERED_MOONCAKE.get() : WEATHERED_MOONCAKE.get();
            case OXIDIZED -> waxed ? WAXED_OXIDIZED_MOONCAKE.get() : OXIDIZED_MOONCAKE.get();
        };
    }

    /** Whole cake → matching piece block (soft or hardened, same weather / wax). */
    public static Block pieceForCake(Block cake) {
        MooncakeWeather weather = weatherOf(cake);
        boolean hardened = MooncakeBlocks.isHardenedCake(cake);
        return byWeather(weather.state(), weather.waxed(), hardened, true);
    }

    public static MooncakeWeather weatherOf(Block block) {
        if (block instanceof WeatheringMooncakeBlock weathering) {
            return new MooncakeWeather(weathering.getAge(), false);
        }
        if (block instanceof WaxedMooncakeBlock waxed) {
            return new MooncakeWeather(waxed.getWeatherState(), true);
        }
        return ModHardenedBlocks.weatherOf(block);
    }

    private static BlockBehaviour.Properties cakeProps(MapColor color, boolean randomTicks, boolean hardened) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .mapColor(color)
                .strength(hardened ? 1.5F : 0.5F)
                .sound(hardened ? SoundType.STONE : SoundType.WOOL)
                .forceSolidOff()
                .noOcclusion();
        if (randomTicks) {
            properties.randomTicks();
        }
        return properties;
    }
}
