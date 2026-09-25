package com.mooncake.blockentity;

import com.mooncake.component.MooncakeCrust;
import com.mooncake.component.MooncakeFillings;
import com.mooncake.config.MooncakeConfig;
import com.mooncake.registry.ModBlockEntities;
import com.mooncake.registry.ModItems;
import com.mooncake.registry.ModTags;
import com.mooncake.util.FillingHelper;
import com.mooncake.util.MooncakeSeriesGuard;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Place dough then fillings; empty-hand take → raw mooncake; sneak-empty → remove last item.
 * Stacks insert as many fillings as capacity allows.
 */
public class MooncakeWorkbenchBlockEntity extends BlockEntity {
    private ItemStack dough = ItemStack.EMPTY;
    private final List<ItemStack> fillings = new ArrayList<>();

    public MooncakeWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MOONCAKE_WORKBENCH.get(), pos, state);
    }

    public ItemStack getDough() {
        return dough;
    }

    public List<ItemStack> getFillings() {
        return List.copyOf(fillings);
    }

    /** Items shown above the workbench: one rendered item per actual count. */
    public List<ItemStack> getDisplayItems() {
        List<ItemStack> items = new ArrayList<>();
        appendByCount(items, dough);
        for (ItemStack filling : fillings) {
            appendByCount(items, filling);
        }
        return items;
    }

    private static void appendByCount(List<ItemStack> items, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        int count = Math.max(1, stack.getCount());
        for (int i = 0; i < count; i++) {
            items.add(stack.copyWithCount(1));
        }
    }

    public int fillingCount() {
        int count = 0;
        for (ItemStack stack : fillings) {
            count += Math.max(1, stack.getCount());
        }
        return count;
    }

    public boolean hasDough() {
        return !dough.isEmpty();
    }

    public boolean canTakeRaw() {
        return hasDough();
    }

    /** Insert held stack: dough first, then fillings (whole stack up to capacity). */
    public boolean tryInsert(ItemStack stack, Player player) {
        if (stack.isEmpty()) {
            return false;
        }
        // New dough while a mooncake is already staged → finish previous into inventory first.
        if (hasDough() && stack.is(ModTags.DOUGHS)) {
            if (!tryTakeRaw(player)) {
                return false;
            }
        }
        if (!hasDough()) {
            if (!stack.is(ModTags.DOUGHS)) {
                return false;
            }
            dough = stack.copyWithCount(1);
            consume(stack, player, 1);
            playInsert();
            setChangedAndSync();
            return true;
        }
        if (MooncakeSeriesGuard.isBlockedAsIngredient(stack)) {
            MooncakeSeriesGuard.warnPlayer(player);
            return false;
        }
        if (!FillingHelper.isValidFilling(stack)) {
            return false;
        }
        int room = MooncakeConfig.maxFillings() - fillingCount();
        if (room <= 0) {
            return false;
        }
        int toPlace = Math.min(stack.getCount(), room);
        if (toPlace <= 0) {
            return false;
        }
        ItemStack placed = stack.copyWithCount(toPlace);
        boolean merged = false;
        for (ItemStack existing : fillings) {
            if (ItemStack.isSameItemSameComponents(existing, placed)) {
                existing.setCount(existing.getCount() + toPlace);
                merged = true;
                break;
            }
        }
        if (!merged) {
            fillings.add(placed);
        }
        consume(stack, player, toPlace);
        playInsert();
        setChangedAndSync();
        return true;
    }

    /** Preview of the raw mooncake that would be taken now. */
    public ItemStack createResultPreview() {
        if (!hasDough()) {
            return ItemStack.EMPTY;
        }
        ItemStack raw = new ItemStack(ModItems.RAW_MOONCAKE.get());
        MooncakeCrust.set(raw, MooncakeCrust.get(dough));
        List<ItemStack> fillingItems = new ArrayList<>();
        for (ItemStack stack : fillings) {
            fillingItems.add(stack.copy());
        }
        MooncakeFillings.set(raw, MooncakeFillings.of(fillingItems));
        return raw;
    }

    /** Sneak extract: last filling, or dough if no fillings. */
    public boolean tryExtractLast(Player player) {
        if (!fillings.isEmpty()) {
            ItemStack last = fillings.get(fillings.size() - 1);
            giveOrDrop(player, last.copyWithCount(1));
            if (last.getCount() > 1) {
                last.shrink(1);
            } else {
                fillings.remove(fillings.size() - 1);
            }
            playExtract();
            setChangedAndSync();
            return true;
        }
        if (!dough.isEmpty()) {
            giveOrDrop(player, dough);
            dough = ItemStack.EMPTY;
            playExtract();
            setChangedAndSync();
            return true;
        }
        return false;
    }

    /** Empty-hand take: assemble raw mooncake from dough + fillings. */
    public boolean tryTakeRaw(Player player) {
        if (!canTakeRaw()) {
            return false;
        }
        ItemStack raw = new ItemStack(ModItems.RAW_MOONCAKE.get());
        MooncakeCrust.set(raw, MooncakeCrust.get(dough));
        List<ItemStack> fillingItems = new ArrayList<>();
        for (ItemStack stack : fillings) {
            fillingItems.add(stack.copy());
        }
        MooncakeFillings.set(raw, MooncakeFillings.of(fillingItems));

        dough = ItemStack.EMPTY;
        fillings.clear();
        giveOrDrop(player, raw);
        playExtract();
        setChangedAndSync();
        return true;
    }

    public List<ItemStack> getDropStacks() {
        List<ItemStack> drops = new ArrayList<>();
        if (!dough.isEmpty()) {
            drops.add(dough.copy());
        }
        for (ItemStack stack : fillings) {
            drops.add(stack.copy());
        }
        return drops;
    }

    private static void consume(ItemStack stack, Player player, int amount) {
        if (!player.hasInfiniteMaterials()) {
            stack.shrink(amount);
        }
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private void playInsert() {
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.4F, 1.1F);
        }
    }

    private void playExtract() {
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.4F, 1.0F);
        }
    }

    private void setChangedAndSync() {
        setChanged();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.getServer().getPlayerList().broadcast(
                    null,
                    worldPosition.getX(),
                    worldPosition.getY(),
                    worldPosition.getZ(),
                    64.0D,
                    serverLevel.dimension(),
                    ClientboundBlockEntityDataPacket.create(this)
            );
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!dough.isEmpty()) {
            tag.put("Dough", dough.save(registries));
        }
        ListTag fillingsTag = new ListTag();
        for (ItemStack stack : fillings) {
            fillingsTag.add(stack.save(registries));
        }
        tag.put("Fillings", fillingsTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        dough = ItemStack.EMPTY;
        if (tag.contains("Dough")) {
            dough = ItemStack.parse(registries, tag.getCompound("Dough")).orElse(ItemStack.EMPTY);
        }
        fillings.clear();
        ListTag fillingsTag = tag.getList("Fillings", Tag.TAG_COMPOUND);
        for (int i = 0; i < fillingsTag.size(); i++) {
            ItemStack.parse(registries, fillingsTag.getCompound(i)).ifPresent(fillings::add);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
