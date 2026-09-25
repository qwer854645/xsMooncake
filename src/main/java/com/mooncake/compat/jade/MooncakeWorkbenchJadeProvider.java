package com.mooncake.compat.jade;

import com.mooncake.MooncakeMod;
import com.mooncake.block.MooncakeWorkbenchBlock;
import com.mooncake.blockentity.MooncakeWorkbenchBlockEntity;
import com.mooncake.util.FillingHelper;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.config.IPluginConfig;

public enum MooncakeWorkbenchJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "mooncake_workbench");

    public static void registerCommon(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(INSTANCE, MooncakeWorkbenchBlockEntity.class);
    }

    public static void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(INSTANCE, MooncakeWorkbenchBlock.class);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data == null || data.isEmpty()) {
            return;
        }
        // Finished / staged product name — no take-out hints.
        if (data.contains("Result")) {
            ItemStack result = ItemStack.parse(accessor.getLevel().registryAccess(), data.getCompound("Result"))
                    .orElse(ItemStack.EMPTY);
            if (!result.isEmpty()) {
                tooltip.add(result.getHoverName());
            }
        }
        boolean hasFillings = data.contains("Fillings") && !data.getList("Fillings", Tag.TAG_COMPOUND).isEmpty();
        if (hasFillings) {
            ListTag list = data.getList("Fillings", Tag.TAG_COMPOUND);
            MutableComponent joined = Component.empty();
            boolean first = true;
            for (int i = 0; i < list.size(); i++) {
                ItemStack stack = ItemStack.parse(accessor.getLevel().registryAccess(), list.getCompound(i))
                        .orElse(ItemStack.EMPTY);
                if (stack.isEmpty()) {
                    continue;
                }
                if (!first) {
                    joined.append(Component.translatable("item.mooncake.filling_separator"));
                }
                joined.append(FillingHelper.fillingLabel(stack));
                first = false;
            }
            if (!first) {
                tooltip.add(Component.translatable("tooltip.mooncake.workbench.fillings", joined));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof MooncakeWorkbenchBlockEntity bench)) {
            return;
        }
        ItemStack preview = bench.createResultPreview();
        if (!preview.isEmpty()) {
            data.put("Result", preview.save(accessor.getLevel().registryAccess()));
        }
        List<ItemStack> fillings = bench.getFillings();
        if (!fillings.isEmpty()) {
            ListTag list = new ListTag();
            for (ItemStack stack : fillings) {
                list.add(stack.save(accessor.getLevel().registryAccess()));
            }
            data.put("Fillings", list);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
