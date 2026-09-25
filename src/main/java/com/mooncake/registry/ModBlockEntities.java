package com.mooncake.registry;

import com.mooncake.MooncakeMod;
import com.mooncake.blockentity.MixingBowlBlockEntity;
import com.mooncake.blockentity.MooncakeBlockEntity;
import com.mooncake.blockentity.MooncakeNamingStandBlockEntity;
import com.mooncake.blockentity.MooncakeWorkbenchBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MooncakeMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MooncakeBlockEntity>> MOONCAKE =
            BLOCK_ENTITIES.register("mooncake", () -> BlockEntityType.Builder.of(
                    MooncakeBlockEntity::new,
                    allMooncakeBlocks()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MixingBowlBlockEntity>> MIXING_BOWL =
            BLOCK_ENTITIES.register("mixing_bowl", () -> BlockEntityType.Builder.of(
                    MixingBowlBlockEntity::new,
                    ModBlocks.MIXING_BOWL.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MooncakeWorkbenchBlockEntity>> MOONCAKE_WORKBENCH =
            BLOCK_ENTITIES.register("mooncake_workbench", () -> BlockEntityType.Builder.of(
                    MooncakeWorkbenchBlockEntity::new,
                    ModBlocks.MOONCAKE_WORKBENCH.get()
            ).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MooncakeNamingStandBlockEntity>> MOONCAKE_NAMING_STAND =
            BLOCK_ENTITIES.register("mooncake_naming_stand", () -> BlockEntityType.Builder.of(
                    MooncakeNamingStandBlockEntity::new,
                    ModBlocks.MOONCAKE_NAMING_STAND.get()
            ).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }

    private static Block[] allMooncakeBlocks() {
        List<Block> blocks = new ArrayList<>();
        blocks.add(ModBlocks.MOONCAKE.get());
        blocks.add(ModBlocks.EXPOSED_MOONCAKE.get());
        blocks.add(ModBlocks.WEATHERED_MOONCAKE.get());
        blocks.add(ModBlocks.OXIDIZED_MOONCAKE.get());
        blocks.add(ModBlocks.WAXED_MOONCAKE.get());
        blocks.add(ModBlocks.WAXED_EXPOSED_MOONCAKE.get());
        blocks.add(ModBlocks.WAXED_WEATHERED_MOONCAKE.get());
        blocks.add(ModBlocks.WAXED_OXIDIZED_MOONCAKE.get());
        blocks.add(ModBlocks.MOONCAKE_PIECE.get());
        blocks.add(ModBlocks.EXPOSED_MOONCAKE_PIECE.get());
        blocks.add(ModBlocks.WEATHERED_MOONCAKE_PIECE.get());
        blocks.add(ModBlocks.OXIDIZED_MOONCAKE_PIECE.get());
        blocks.add(ModBlocks.WAXED_MOONCAKE_PIECE.get());
        blocks.add(ModBlocks.WAXED_EXPOSED_MOONCAKE_PIECE.get());
        blocks.add(ModBlocks.WAXED_WEATHERED_MOONCAKE_PIECE.get());
        blocks.add(ModBlocks.WAXED_OXIDIZED_MOONCAKE_PIECE.get());
        blocks.add(ModBlocks.HARDENED_MOONCAKE.get());
        blocks.add(ModBlocks.EXPOSED_HARDENED_MOONCAKE.get());
        blocks.add(ModBlocks.WEATHERED_HARDENED_MOONCAKE.get());
        blocks.add(ModBlocks.OXIDIZED_HARDENED_MOONCAKE.get());
        blocks.add(ModBlocks.WAXED_HARDENED_MOONCAKE.get());
        blocks.add(ModBlocks.WAXED_EXPOSED_HARDENED_MOONCAKE.get());
        blocks.add(ModBlocks.WAXED_WEATHERED_HARDENED_MOONCAKE.get());
        blocks.add(ModBlocks.WAXED_OXIDIZED_HARDENED_MOONCAKE.get());
        blocks.add(ModBlocks.HARDENED_MOONCAKE_PIECE.get());
        blocks.add(ModBlocks.EXPOSED_HARDENED_MOONCAKE_PIECE.get());
        blocks.add(ModBlocks.WEATHERED_HARDENED_MOONCAKE_PIECE.get());
        blocks.add(ModBlocks.OXIDIZED_HARDENED_MOONCAKE_PIECE.get());
        blocks.add(ModBlocks.WAXED_HARDENED_MOONCAKE_PIECE.get());
        blocks.add(ModBlocks.WAXED_EXPOSED_HARDENED_MOONCAKE_PIECE.get());
        blocks.add(ModBlocks.WAXED_WEATHERED_HARDENED_MOONCAKE_PIECE.get());
        blocks.add(ModBlocks.WAXED_OXIDIZED_HARDENED_MOONCAKE_PIECE.get());
        for (DeferredBlock<? extends Block> deferred : ModHardenedBlocks.ALL_BLOCKS) {
            blocks.add(deferred.get());
        }
        return blocks.toArray(Block[]::new);
    }
}
