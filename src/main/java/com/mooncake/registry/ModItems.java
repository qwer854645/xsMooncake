package com.mooncake.registry;

import com.mooncake.MooncakeMod;
import com.mooncake.block.WaxedMooncakeBlock;
import com.mooncake.item.MooncakeBlockItem;
import com.mooncake.item.MooncakeDoughItem;
import com.mooncake.item.MooncakePieceItem;
import com.mooncake.item.PlasticWrapItem;
import com.mooncake.item.RawMooncakeItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MooncakeMod.MOD_ID);

    private static final FoodProperties MOONCAKE_FOOD = new FoodProperties.Builder()
            .nutrition(4)
            .saturationModifier(0.3F)
            .alwaysEdible()
            .build();

    private static final FoodProperties MOONCAKE_PIECE_FOOD = new FoodProperties.Builder()
            .nutrition(1)
            .saturationModifier(0.1F)
            .alwaysEdible()
            .fast()
            .build();

    public static final DeferredItem<BlockItem> MIXING_BOWL = ITEMS.register(
            "mixing_bowl",
            () -> new BlockItem(ModBlocks.MIXING_BOWL.get(), new Item.Properties())
    );

    public static final DeferredItem<BlockItem> MOONCAKE_WORKBENCH = ITEMS.register(
            "mooncake_workbench",
            () -> new BlockItem(ModBlocks.MOONCAKE_WORKBENCH.get(), new Item.Properties())
    );

    public static final DeferredItem<BlockItem> MOONCAKE_NAMING_STAND = ITEMS.register(
            "mooncake_naming_stand",
            () -> new BlockItem(ModBlocks.MOONCAKE_NAMING_STAND.get(), new Item.Properties())
    );

    public static final DeferredItem<Item> INCOMPLETE_MOONCAKE_DOUGH = ITEMS.register(
            "incomplete_mooncake_dough",
            () -> new Item(new Item.Properties())
    );

    public static final DeferredItem<MooncakeDoughItem> MOONCAKE_DOUGH = ITEMS.register(
            "mooncake_dough",
            () -> new MooncakeDoughItem(new Item.Properties())
    );

    public static final DeferredItem<RawMooncakeItem> RAW_MOONCAKE = ITEMS.register(
            "raw_mooncake",
            () -> new RawMooncakeItem(new Item.Properties())
    );

    public static final DeferredItem<PlasticWrapItem> PLASTIC_WRAP = ITEMS.register(
            "plastic_wrap",
            () -> new PlasticWrapItem(new Item.Properties())
    );

    public static final DeferredItem<MooncakeBlockItem> MOONCAKE =
            registerMooncake("mooncake", ModBlocks.MOONCAKE, MOONCAKE_FOOD);
    public static final DeferredItem<MooncakeBlockItem> EXPOSED_MOONCAKE =
            registerMooncake("exposed_mooncake", ModBlocks.EXPOSED_MOONCAKE, MOONCAKE_FOOD);
    public static final DeferredItem<MooncakeBlockItem> WEATHERED_MOONCAKE =
            registerMooncake("weathered_mooncake", ModBlocks.WEATHERED_MOONCAKE, MOONCAKE_FOOD);
    public static final DeferredItem<MooncakeBlockItem> OXIDIZED_MOONCAKE =
            registerMooncake("oxidized_mooncake", ModBlocks.OXIDIZED_MOONCAKE, MOONCAKE_FOOD);
    public static final DeferredItem<MooncakeBlockItem> WAXED_MOONCAKE =
            registerMooncake("waxed_mooncake", ModBlocks.WAXED_MOONCAKE, MOONCAKE_FOOD);
    public static final DeferredItem<MooncakeBlockItem> WAXED_EXPOSED_MOONCAKE =
            registerMooncake("waxed_exposed_mooncake", ModBlocks.WAXED_EXPOSED_MOONCAKE, MOONCAKE_FOOD);
    public static final DeferredItem<MooncakeBlockItem> WAXED_WEATHERED_MOONCAKE =
            registerMooncake("waxed_weathered_mooncake", ModBlocks.WAXED_WEATHERED_MOONCAKE, MOONCAKE_FOOD);
    public static final DeferredItem<MooncakeBlockItem> WAXED_OXIDIZED_MOONCAKE =
            registerMooncake("waxed_oxidized_mooncake", ModBlocks.WAXED_OXIDIZED_MOONCAKE, MOONCAKE_FOOD);

    public static final DeferredItem<MooncakePieceItem> MOONCAKE_PIECE =
            registerPiece("mooncake_piece", ModBlocks.MOONCAKE_PIECE);
    public static final DeferredItem<MooncakePieceItem> EXPOSED_MOONCAKE_PIECE =
            registerPiece("exposed_mooncake_piece", ModBlocks.EXPOSED_MOONCAKE_PIECE);
    public static final DeferredItem<MooncakePieceItem> WEATHERED_MOONCAKE_PIECE =
            registerPiece("weathered_mooncake_piece", ModBlocks.WEATHERED_MOONCAKE_PIECE);
    public static final DeferredItem<MooncakePieceItem> OXIDIZED_MOONCAKE_PIECE =
            registerPiece("oxidized_mooncake_piece", ModBlocks.OXIDIZED_MOONCAKE_PIECE);
    public static final DeferredItem<MooncakePieceItem> WAXED_MOONCAKE_PIECE =
            registerPiece("waxed_mooncake_piece", ModBlocks.WAXED_MOONCAKE_PIECE);
    public static final DeferredItem<MooncakePieceItem> WAXED_EXPOSED_MOONCAKE_PIECE =
            registerPiece("waxed_exposed_mooncake_piece", ModBlocks.WAXED_EXPOSED_MOONCAKE_PIECE);
    public static final DeferredItem<MooncakePieceItem> WAXED_WEATHERED_MOONCAKE_PIECE =
            registerPiece("waxed_weathered_mooncake_piece", ModBlocks.WAXED_WEATHERED_MOONCAKE_PIECE);
    public static final DeferredItem<MooncakePieceItem> WAXED_OXIDIZED_MOONCAKE_PIECE =
            registerPiece("waxed_oxidized_mooncake_piece", ModBlocks.WAXED_OXIDIZED_MOONCAKE_PIECE);

    public static final DeferredItem<MooncakeBlockItem> HARDENED_MOONCAKE =
            registerMooncake("hardened_mooncake", ModBlocks.HARDENED_MOONCAKE, null);
    public static final DeferredItem<MooncakeBlockItem> EXPOSED_HARDENED_MOONCAKE =
            registerMooncake("exposed_hardened_mooncake", ModBlocks.EXPOSED_HARDENED_MOONCAKE, null);
    public static final DeferredItem<MooncakeBlockItem> WEATHERED_HARDENED_MOONCAKE =
            registerMooncake("weathered_hardened_mooncake", ModBlocks.WEATHERED_HARDENED_MOONCAKE, null);
    public static final DeferredItem<MooncakeBlockItem> OXIDIZED_HARDENED_MOONCAKE =
            registerMooncake("oxidized_hardened_mooncake", ModBlocks.OXIDIZED_HARDENED_MOONCAKE, null);
    public static final DeferredItem<MooncakeBlockItem> WAXED_HARDENED_MOONCAKE =
            registerMooncake("waxed_hardened_mooncake", ModBlocks.WAXED_HARDENED_MOONCAKE, null);
    public static final DeferredItem<MooncakeBlockItem> WAXED_EXPOSED_HARDENED_MOONCAKE =
            registerMooncake("waxed_exposed_hardened_mooncake", ModBlocks.WAXED_EXPOSED_HARDENED_MOONCAKE, null);
    public static final DeferredItem<MooncakeBlockItem> WAXED_WEATHERED_HARDENED_MOONCAKE =
            registerMooncake("waxed_weathered_hardened_mooncake", ModBlocks.WAXED_WEATHERED_HARDENED_MOONCAKE, null);
    public static final DeferredItem<MooncakeBlockItem> WAXED_OXIDIZED_HARDENED_MOONCAKE =
            registerMooncake("waxed_oxidized_hardened_mooncake", ModBlocks.WAXED_OXIDIZED_HARDENED_MOONCAKE, null);

    public static final DeferredItem<MooncakeBlockItem> HARDENED_MOONCAKE_PIECE =
            registerMooncake("hardened_mooncake_piece", ModBlocks.HARDENED_MOONCAKE_PIECE, null);
    public static final DeferredItem<MooncakeBlockItem> EXPOSED_HARDENED_MOONCAKE_PIECE =
            registerMooncake("exposed_hardened_mooncake_piece", ModBlocks.EXPOSED_HARDENED_MOONCAKE_PIECE, null);
    public static final DeferredItem<MooncakeBlockItem> WEATHERED_HARDENED_MOONCAKE_PIECE =
            registerMooncake("weathered_hardened_mooncake_piece", ModBlocks.WEATHERED_HARDENED_MOONCAKE_PIECE, null);
    public static final DeferredItem<MooncakeBlockItem> OXIDIZED_HARDENED_MOONCAKE_PIECE =
            registerMooncake("oxidized_hardened_mooncake_piece", ModBlocks.OXIDIZED_HARDENED_MOONCAKE_PIECE, null);
    public static final DeferredItem<MooncakeBlockItem> WAXED_HARDENED_MOONCAKE_PIECE =
            registerMooncake("waxed_hardened_mooncake_piece", ModBlocks.WAXED_HARDENED_MOONCAKE_PIECE, null);
    public static final DeferredItem<MooncakeBlockItem> WAXED_EXPOSED_HARDENED_MOONCAKE_PIECE =
            registerMooncake("waxed_exposed_hardened_mooncake_piece", ModBlocks.WAXED_EXPOSED_HARDENED_MOONCAKE_PIECE, null);
    public static final DeferredItem<MooncakeBlockItem> WAXED_WEATHERED_HARDENED_MOONCAKE_PIECE =
            registerMooncake("waxed_weathered_hardened_mooncake_piece", ModBlocks.WAXED_WEATHERED_HARDENED_MOONCAKE_PIECE, null);
    public static final DeferredItem<MooncakeBlockItem> WAXED_OXIDIZED_HARDENED_MOONCAKE_PIECE =
            registerMooncake("waxed_oxidized_hardened_mooncake_piece", ModBlocks.WAXED_OXIDIZED_HARDENED_MOONCAKE_PIECE, null);

    private ModItems() {
    }

    private static DeferredItem<MooncakeBlockItem> registerMooncake(
            String name, DeferredBlock<? extends Block> block, @javax.annotation.Nullable FoodProperties food
    ) {
        return ITEMS.register(name, () -> {
            Item.Properties props = new Item.Properties();
            if (food != null) {
                props.food(food);
            }
            return new MooncakeBlockItem(block.get(), props);
        });
    }

    private static DeferredItem<MooncakePieceItem> registerPiece(String name, DeferredBlock<? extends Block> block) {
        return ITEMS.register(name, () -> new MooncakePieceItem(block.get(), new Item.Properties().food(MOONCAKE_PIECE_FOOD)));
    }

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }

    public static boolean isMooncakeItem(ItemStack stack) {
        return stack.getItem() instanceof MooncakeBlockItem;
    }

    public static boolean isWaxedMooncakeItem(Item item) {
        return item instanceof MooncakeBlockItem blockItem && blockItem.getBlock() instanceof WaxedMooncakeBlock;
    }
}
