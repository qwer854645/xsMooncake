package com.mooncake.compat.jade;

import com.mooncake.MooncakeMod;
import com.mooncake.block.HardenedBuildingBlockTypes;
import com.mooncake.block.WaxedMooncakeBlock;
import com.mooncake.block.WeatheringMooncakeBlock;
import com.mooncake.blockentity.MooncakeBlockEntity;
import com.mooncake.component.MooncakeCrust;
import com.mooncake.component.MooncakeFillings;
import com.mooncake.registry.ModBlocks;
import com.mooncake.registry.ModHardenedBlocks;
import com.mooncake.util.MooncakeNames;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;

@WailaPlugin
public class MooncakeJadePlugin implements IWailaPlugin {
    public static final ResourceLocation DISPLAY_NAME =
            ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "display_name");

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(MooncakeNameProvider.INSTANCE, MooncakeBlockEntity.class);
        MixingBowlJadeProvider.registerCommon(registration);
        MooncakeWorkbenchJadeProvider.registerCommon(registration);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(MooncakeNameProvider.INSTANCE, WeatheringMooncakeBlock.class);
        registration.registerBlockComponent(MooncakeNameProvider.INSTANCE, WaxedMooncakeBlock.class);
        registration.registerBlockComponent(MooncakeNameProvider.INSTANCE, HardenedBuildingBlockTypes.WeatheringFull.class);
        registration.registerBlockComponent(MooncakeNameProvider.INSTANCE, HardenedBuildingBlockTypes.WaxedFull.class);
        registration.registerBlockComponent(MooncakeNameProvider.INSTANCE, HardenedBuildingBlockTypes.WeatheringSlab.class);
        registration.registerBlockComponent(MooncakeNameProvider.INSTANCE, HardenedBuildingBlockTypes.WaxedSlab.class);
        registration.registerBlockComponent(MooncakeNameProvider.INSTANCE, HardenedBuildingBlockTypes.WeatheringStairs.class);
        registration.registerBlockComponent(MooncakeNameProvider.INSTANCE, HardenedBuildingBlockTypes.WaxedStairs.class);
        registration.registerBlockComponent(MooncakeNameProvider.INSTANCE, HardenedBuildingBlockTypes.WeatheringDoor.class);
        registration.registerBlockComponent(MooncakeNameProvider.INSTANCE, HardenedBuildingBlockTypes.WaxedDoor.class);
        MixingBowlJadeProvider.registerClient(registration);
        MooncakeWorkbenchJadeProvider.registerClient(registration);

        // Prefer picked ItemStack (includes fillings/crust) for icon + default naming path.
        for (Block block : new Block[] {
                ModBlocks.MOONCAKE.get(),
                ModBlocks.EXPOSED_MOONCAKE.get(),
                ModBlocks.WEATHERED_MOONCAKE.get(),
                ModBlocks.OXIDIZED_MOONCAKE.get(),
                ModBlocks.WAXED_MOONCAKE.get(),
                ModBlocks.WAXED_EXPOSED_MOONCAKE.get(),
                ModBlocks.WAXED_WEATHERED_MOONCAKE.get(),
                ModBlocks.WAXED_OXIDIZED_MOONCAKE.get(),
                ModBlocks.HARDENED_MOONCAKE.get(),
                ModBlocks.EXPOSED_HARDENED_MOONCAKE.get(),
                ModBlocks.WEATHERED_HARDENED_MOONCAKE.get(),
                ModBlocks.OXIDIZED_HARDENED_MOONCAKE.get(),
                ModBlocks.WAXED_HARDENED_MOONCAKE.get(),
                ModBlocks.WAXED_EXPOSED_HARDENED_MOONCAKE.get(),
                ModBlocks.WAXED_WEATHERED_HARDENED_MOONCAKE.get(),
                ModBlocks.WAXED_OXIDIZED_HARDENED_MOONCAKE.get()
        }) {
            registration.usePickedResult(block);
        }
        for (DeferredBlock<? extends Block> deferred : ModHardenedBlocks.ALL_BLOCKS) {
            registration.usePickedResult(deferred.get());
        }
    }

    public enum MooncakeNameProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
        INSTANCE;

        private static final String FILLINGS_KEY = "MooncakeFillings";
        private static final String CRUST_KEY = "MooncakeCrust";

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            MooncakeFillings fillings = readFillings(accessor);
            MooncakeCrust crust = readCrust(accessor);
            Component name = MooncakeNames.forBlock(accessor.getBlockState(), fillings, crust);
            tooltip.replace(JadeIds.CORE_OBJECT_NAME, name);
        }

        @Override
        public void appendServerData(CompoundTag data, BlockAccessor accessor) {
            if (!(accessor.getBlockEntity() instanceof MooncakeBlockEntity be)) {
                return;
            }
            MooncakeFillings fillings = be.getFillings();
            if (!fillings.isEmpty()) {
                Tag encoded = MooncakeFillings.CODEC
                        .encodeStart(accessor.nbtOps(), fillings)
                        .result()
                        .orElse(null);
                if (encoded != null) {
                    data.put(FILLINGS_KEY, encoded);
                }
            }
            MooncakeCrust crust = be.getCrust();
            if (!crust.isEmpty()) {
                Tag encoded = MooncakeCrust.CODEC
                        .encodeStart(accessor.nbtOps(), crust)
                        .result()
                        .orElse(null);
                if (encoded != null) {
                    data.put(CRUST_KEY, encoded);
                }
            }
        }

        private static MooncakeFillings readFillings(BlockAccessor accessor) {
            CompoundTag data = accessor.getServerData();
            if (data != null && data.contains(FILLINGS_KEY)) {
                return MooncakeFillings.CODEC
                        .parse(accessor.nbtOps(), data.get(FILLINGS_KEY))
                        .result()
                        .orElse(MooncakeFillings.EMPTY);
            }
            if (accessor.getBlockEntity() instanceof MooncakeBlockEntity be) {
                return be.getFillings();
            }
            return MooncakeFillings.EMPTY;
        }

        private static MooncakeCrust readCrust(BlockAccessor accessor) {
            CompoundTag data = accessor.getServerData();
            if (data != null && data.contains(CRUST_KEY)) {
                return MooncakeCrust.CODEC
                        .parse(accessor.nbtOps(), data.get(CRUST_KEY))
                        .result()
                        .orElse(MooncakeCrust.EMPTY);
            }
            if (accessor.getBlockEntity() instanceof MooncakeBlockEntity be) {
                return be.getCrust();
            }
            return MooncakeCrust.EMPTY;
        }

        @Override
        public ResourceLocation getUid() {
            return DISPLAY_NAME;
        }

        @Override
        public int getDefaultPriority() {
            // After Jade's object name provider so replace() can find CORE_OBJECT_NAME.
            return 100;
        }
    }
}
