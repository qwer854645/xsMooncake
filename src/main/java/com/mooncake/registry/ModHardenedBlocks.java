package com.mooncake.registry;

import com.mooncake.MooncakeMod;
import com.mooncake.block.HardenedBuildingBlockTypes;
import com.mooncake.component.MooncakeWeather;
import com.mooncake.item.HardenedBuildingBlockItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Hardened mooncake building blocks — copper-like set per weather / wax stage:
 * full block, cut, cut slab, cut stairs, door.
 */
public final class ModHardenedBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MooncakeMod.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MooncakeMod.MOD_ID);

    public static final List<DeferredBlock<? extends Block>> ALL_BLOCKS = new ArrayList<>();
    public static final List<DeferredItem<? extends Item>> ALL_ITEMS = new ArrayList<>();

    public static final Stage UNAFFECTED = stage("", MapColor.TERRACOTTA_ORANGE, WeatheringCopper.WeatherState.UNAFFECTED, true);
    public static final Stage EXPOSED = stage("exposed_", MapColor.TERRACOTTA_LIGHT_GRAY, WeatheringCopper.WeatherState.EXPOSED, true);
    public static final Stage WEATHERED = stage("weathered_", MapColor.WARPED_STEM, WeatheringCopper.WeatherState.WEATHERED, true);
    public static final Stage OXIDIZED = stage("oxidized_", MapColor.WARPED_NYLIUM, WeatheringCopper.WeatherState.OXIDIZED, true);

    public static final Stage WAXED_UNAFFECTED = stage("waxed_", MapColor.TERRACOTTA_ORANGE, WeatheringCopper.WeatherState.UNAFFECTED, false);
    public static final Stage WAXED_EXPOSED = stage("waxed_exposed_", MapColor.TERRACOTTA_LIGHT_GRAY, WeatheringCopper.WeatherState.EXPOSED, false);
    public static final Stage WAXED_WEATHERED = stage("waxed_weathered_", MapColor.WARPED_STEM, WeatheringCopper.WeatherState.WEATHERED, false);
    public static final Stage WAXED_OXIDIZED = stage("waxed_oxidized_", MapColor.WARPED_NYLIUM, WeatheringCopper.WeatherState.OXIDIZED, false);

    public static final Stage[] STAGES = {
            UNAFFECTED, EXPOSED, WEATHERED, OXIDIZED,
            WAXED_UNAFFECTED, WAXED_EXPOSED, WAXED_WEATHERED, WAXED_OXIDIZED
    };

    private ModHardenedBlocks() {
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
    }

    public static MooncakeWeather weatherOf(Block block) {
        for (Stage stage : STAGES) {
            if (stage.contains(block)) {
                return new MooncakeWeather(stage.weatherState, !stage.weathering);
            }
        }
        return MooncakeWeather.DEFAULT;
    }

    public static Block blockFor(MooncakeWeather weather) {
        for (Stage stage : STAGES) {
            boolean waxed = !stage.weathering;
            if (stage.weatherState == weather.state() && waxed == weather.waxed()) {
                return stage.block().get();
            }
        }
        return UNAFFECTED.block().get();
    }

    public static boolean isFamily(Block block) {
        for (Stage stage : STAGES) {
            if (stage.contains(block)) {
                return true;
            }
        }
        return false;
    }

    /** Same product (block / cut / slab / stairs / door) at the waxed twin stage. */
    public static Optional<Block> waxedCounterpart(Block block) {
        for (Stage stage : STAGES) {
            if (!stage.weathering || !stage.contains(block)) {
                continue;
            }
            Stage waxed = waxedStage(stage.weatherState);
            if (waxed == null) {
                return Optional.empty();
            }
            if (block == stage.block.get()) {
                return Optional.of(waxed.block.get());
            }
            if (block == stage.cut.get()) {
                return Optional.of(waxed.cut.get());
            }
            if (block == stage.slab.get()) {
                return Optional.of(waxed.slab.get());
            }
            if (block == stage.stairs.get()) {
                return Optional.of(waxed.stairs.get());
            }
            if (block == stage.door.get()) {
                return Optional.of(waxed.door.get());
            }
        }
        return Optional.empty();
    }

    /** Same product at the unwaxed twin stage. */
    public static Optional<Block> unwaxedCounterpart(Block block) {
        for (Stage stage : STAGES) {
            if (stage.weathering || !stage.contains(block)) {
                continue;
            }
            Stage unwaxed = unwaxedStage(stage.weatherState);
            if (unwaxed == null) {
                return Optional.empty();
            }
            if (block == stage.block.get()) {
                return Optional.of(unwaxed.block.get());
            }
            if (block == stage.cut.get()) {
                return Optional.of(unwaxed.cut.get());
            }
            if (block == stage.slab.get()) {
                return Optional.of(unwaxed.slab.get());
            }
            if (block == stage.stairs.get()) {
                return Optional.of(unwaxed.stairs.get());
            }
            if (block == stage.door.get()) {
                return Optional.of(unwaxed.door.get());
            }
        }
        return Optional.empty();
    }

    /** Previous oxidation stage for an unwaxed hardened building block. */
    public static Optional<Block> previousOxidation(Block block) {
        MooncakeWeather weather = weatherOf(block);
        if (weather.waxed()) {
            return Optional.empty();
        }
        WeatheringCopper.WeatherState prev = switch (weather.state()) {
            case UNAFFECTED -> null;
            case EXPOSED -> WeatheringCopper.WeatherState.UNAFFECTED;
            case WEATHERED -> WeatheringCopper.WeatherState.EXPOSED;
            case OXIDIZED -> WeatheringCopper.WeatherState.WEATHERED;
        };
        if (prev == null) {
            return Optional.empty();
        }
        for (Stage stage : STAGES) {
            if (!stage.weathering || stage.weatherState != weather.state() || !stage.contains(block)) {
                continue;
            }
            Stage target = unwaxedStage(prev);
            if (target == null) {
                return Optional.empty();
            }
            if (block == stage.block.get()) {
                return Optional.of(target.block.get());
            }
            if (block == stage.cut.get()) {
                return Optional.of(target.cut.get());
            }
            if (block == stage.slab.get()) {
                return Optional.of(target.slab.get());
            }
            if (block == stage.stairs.get()) {
                return Optional.of(target.stairs.get());
            }
            if (block == stage.door.get()) {
                return Optional.of(target.door.get());
            }
        }
        return Optional.empty();
    }

    private static Stage unwaxedStage(WeatheringCopper.WeatherState weather) {
        for (Stage stage : STAGES) {
            if (stage.weathering && stage.weatherState == weather) {
                return stage;
            }
        }
        return null;
    }

    private static Stage waxedStage(WeatheringCopper.WeatherState weather) {
        for (Stage stage : STAGES) {
            if (!stage.weathering && stage.weatherState == weather) {
                return stage;
            }
        }
        return null;
    }

    private static Stage stage(String prefix, MapColor color, WeatheringCopper.WeatherState weather, boolean weathering) {
        String blockName = prefix + "hardened_mooncake_block";
        String cutName = prefix + "cut_hardened_mooncake";
        String slabName = prefix + "cut_hardened_mooncake_slab";
        String stairsName = prefix + "cut_hardened_mooncake_stairs";
        String doorName = prefix + "hardened_mooncake_door";

        DeferredBlock<Block> block;
        DeferredBlock<Block> cut;
        DeferredBlock<Block> slab;
        DeferredBlock<Block> stairs;
        DeferredBlock<Block> door;

        if (weathering) {
            block = registerBlock(blockName, () -> new HardenedBuildingBlockTypes.WeatheringFull(weather, metalProps(color, true)));
            cut = registerBlock(cutName, () -> new HardenedBuildingBlockTypes.WeatheringFull(weather, metalProps(color, true)));
            slab = registerBlock(slabName, () -> new HardenedBuildingBlockTypes.WeatheringSlab(weather, metalProps(color, true)));
            stairs = registerBlock(stairsName, () -> new HardenedBuildingBlockTypes.WeatheringStairs(
                    weather, cut.get().defaultBlockState(), metalProps(color, true)));
            door = registerBlock(doorName, () -> new HardenedBuildingBlockTypes.WeatheringDoor(
                    BlockSetType.COPPER, weather, metalProps(color, true).noOcclusion()));
        } else {
            block = registerBlock(blockName, () -> new HardenedBuildingBlockTypes.WaxedFull(metalProps(color, false)));
            cut = registerBlock(cutName, () -> new HardenedBuildingBlockTypes.WaxedFull(metalProps(color, false)));
            slab = registerBlock(slabName, () -> new HardenedBuildingBlockTypes.WaxedSlab(metalProps(color, false)));
            stairs = registerBlock(stairsName, () -> new HardenedBuildingBlockTypes.WaxedStairs(
                    cut.get().defaultBlockState(), metalProps(color, false)));
            door = registerBlock(doorName, () -> new HardenedBuildingBlockTypes.WaxedDoor(
                    BlockSetType.COPPER, metalProps(color, false).noOcclusion()));
        }

        registerBlockItem(blockName, block, HardenedBuildingBlockItem.Product.BLOCK);
        registerBlockItem(cutName, cut, HardenedBuildingBlockItem.Product.CUT);
        registerBlockItem(slabName, slab, HardenedBuildingBlockItem.Product.SLAB);
        registerBlockItem(stairsName, stairs, HardenedBuildingBlockItem.Product.STAIRS);
        registerDoorItem(doorName, door);

        return new Stage(weather, weathering, block, cut, slab, stairs, door);
    }

    private static DeferredBlock<Block> registerBlock(String name, java.util.function.Supplier<Block> supplier) {
        DeferredBlock<Block> deferred = BLOCKS.register(name, supplier);
        ALL_BLOCKS.add(deferred);
        return deferred;
    }

    private static void registerBlockItem(
            String name, DeferredBlock<? extends Block> block, HardenedBuildingBlockItem.Product product
    ) {
        DeferredItem<HardenedBuildingBlockItem> item = ITEMS.register(
                name, () -> new HardenedBuildingBlockItem(block.get(), product, new Item.Properties()));
        ALL_ITEMS.add(item);
    }

    private static void registerDoorItem(String name, DeferredBlock<? extends Block> block) {
        DeferredItem<HardenedBuildingBlockItem.Door> item = ITEMS.register(
                name, () -> new HardenedBuildingBlockItem.Door(block.get(), new Item.Properties()));
        ALL_ITEMS.add(item);
    }

    private static BlockBehaviour.Properties metalProps(MapColor color, boolean randomTicks) {
        BlockBehaviour.Properties properties = BlockBehaviour.Properties.of()
                .mapColor(color)
                .requiresCorrectToolForDrops()
                .strength(3.0F, 6.0F)
                .sound(SoundType.COPPER);
        if (randomTicks) {
            properties.randomTicks();
        }
        return properties;
    }

    public record Stage(
            WeatheringCopper.WeatherState weatherState,
            boolean weathering,
            DeferredBlock<? extends Block> block,
            DeferredBlock<? extends Block> cut,
            DeferredBlock<? extends Block> slab,
            DeferredBlock<? extends Block> stairs,
            DeferredBlock<? extends Block> door
    ) {
        boolean contains(Block b) {
            return block.get() == b || cut.get() == b || slab.get() == b || stairs.get() == b || door.get() == b;
        }
    }
}
