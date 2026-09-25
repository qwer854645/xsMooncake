package com.mooncake.blockentity;

import com.mooncake.component.MooncakeInscription;
import com.mooncake.registry.ModBlockEntities;
import com.mooncake.util.MooncakeInscriptionLayout;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MooncakeNamingStandBlockEntity extends BlockEntity {
    private ItemStack mooncake = ItemStack.EMPTY;
    private String pendingName = "";

    public MooncakeNamingStandBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MOONCAKE_NAMING_STAND.get(), pos, state);
    }

    public ItemStack getMooncake() {
        return mooncake;
    }

    public String getPendingName() {
        return pendingName;
    }

    public void setPendingName(String name) {
        this.pendingName = MooncakeInscription.sanitize(name);
        setChangedAndSync();
    }

    public List<ItemStack> getDisplayItems() {
        return mooncake.isEmpty() ? List.of() : List.of(mooncake.copyWithCount(1));
    }

    public boolean tryInsert(ItemStack stack, Player player) {
        if (!mooncake.isEmpty() || !MooncakeInscriptionLayout.isNameableMooncake(stack)) {
            return false;
        }
        mooncake = stack.copyWithCount(1);
        if (!player.hasInfiniteMaterials()) {
            stack.shrink(1);
        }
        playInsert();
        setChangedAndSync();
        return true;
    }

    /** Take mooncake without applying inscription. */
    public boolean tryTakePlain(Player player) {
        if (mooncake.isEmpty()) {
            return false;
        }
        giveOrDrop(player, mooncake);
        mooncake = ItemStack.EMPTY;
        playExtract();
        setChangedAndSync();
        return true;
    }

    /** Take mooncake with stand's pending name stamped as inscription. */
    public boolean tryTakeNamed(Player player) {
        if (mooncake.isEmpty()) {
            return false;
        }
        ItemStack out = mooncake.copy();
        MooncakeInscription.set(out, MooncakeInscription.of(pendingName));
        mooncake = ItemStack.EMPTY;
        giveOrDrop(player, out);
        playExtract();
        setChangedAndSync();
        return true;
    }

    public List<ItemStack> getDropStacks() {
        return mooncake.isEmpty() ? List.of() : List.of(mooncake.copy());
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
        if (!mooncake.isEmpty()) {
            tag.put("Mooncake", mooncake.save(registries));
        }
        if (!pendingName.isEmpty()) {
            tag.putString("PendingName", pendingName);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        mooncake = ItemStack.EMPTY;
        if (tag.contains("Mooncake")) {
            mooncake = ItemStack.parse(registries, tag.getCompound("Mooncake")).orElse(ItemStack.EMPTY);
        }
        pendingName = tag.contains("PendingName") ? MooncakeInscription.sanitize(tag.getString("PendingName")) : "";
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
