package com.mooncake.compat.jade;

import com.mooncake.MooncakeMod;
import com.mooncake.block.MixingBowlBlock;
import com.mooncake.blockentity.MixingBowlBlockEntity;
import com.mooncake.util.CrustHelper;
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

public enum MixingBowlJadeProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    public static final ResourceLocation UID =
            ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "mixing_bowl");

    public static void registerCommon(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(INSTANCE, MixingBowlBlockEntity.class);
    }

    public static void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(INSTANCE, MixingBowlBlock.class);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        if (data == null || data.isEmpty()) {
            return;
        }
        if (data.getBoolean("Mixing")) {
            int progress = data.getInt("MixProgress");
            int time = Math.max(1, data.getInt("MixTime"));
            int percent = Math.min(100, progress * 100 / time);
            tooltip.add(Component.translatable("tooltip.mooncake.mixing_bowl.progress", percent));
            return;
        }
        // Finished product — show the item itself, no “ready to take” hint.
        if (data.contains("Output")) {
            ItemStack output = ItemStack.parse(accessor.getLevel().registryAccess(), data.getCompound("Output"))
                    .orElse(ItemStack.EMPTY);
            if (!output.isEmpty()) {
                tooltip.add(output.getHoverName());
            }
            return;
        }

        int wheat = data.getInt("Wheat");
        int egg = data.getInt("Egg");
        int sugar = data.getInt("Sugar");
        int incomplete = data.getInt("Incomplete");
        boolean hasExtras = data.contains("Extras") && !data.getList("Extras", Tag.TAG_COMPOUND).isEmpty();
        if (wheat == 0 && egg == 0 && sugar == 0 && incomplete == 0 && !hasExtras) {
            return;
        }
        if (incomplete > 0) {
            tooltip.add(Component.translatable("tooltip.mooncake.mixing_bowl.incomplete", incomplete));
        } else if (wheat > 0 || egg > 0 || sugar > 0) {
            tooltip.add(Component.translatable("tooltip.mooncake.mixing_bowl.contents", wheat, egg, sugar));
        }
        if (hasExtras) {
            ListTag extras = data.getList("Extras", Tag.TAG_COMPOUND);
            MutableComponent joined = Component.empty();
            boolean first = true;
            for (int i = 0; i < extras.size(); i++) {
                ItemStack stack = ItemStack.parse(accessor.getLevel().registryAccess(), extras.getCompound(i))
                        .orElse(ItemStack.EMPTY);
                if (stack.isEmpty()) {
                    continue;
                }
                if (!first) {
                    joined.append(Component.translatable("item.mooncake.filling_separator"));
                }
                joined.append(CrustHelper.crustLabel(stack));
                first = false;
            }
            if (!first) {
                tooltip.add(Component.translatable("tooltip.mooncake.mixing_bowl.extras", joined));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof MixingBowlBlockEntity bowl)) {
            return;
        }
        data.putInt("Wheat", bowl.getWheat());
        data.putInt("Egg", bowl.getEgg());
        data.putInt("Sugar", bowl.getSugar());
        data.putInt("Incomplete", bowl.getIncomplete());
        data.putInt("MixProgress", bowl.getMixProgress());
        data.putInt("MixTime", bowl.getMixTime());
        data.putBoolean("Mixing", bowl.isMixing());
        if (!bowl.getOutput().isEmpty()) {
            data.put("Output", bowl.getOutput().save(accessor.getLevel().registryAccess()));
        }
        List<ItemStack> extras = bowl.getExtras();
        if (!extras.isEmpty()) {
            ListTag list = new ListTag();
            for (ItemStack stack : extras) {
                list.add(stack.save(accessor.getLevel().registryAccess()));
            }
            data.put("Extras", list);
        }
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }
}
