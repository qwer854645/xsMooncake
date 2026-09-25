package com.mooncake.blockentity;

import com.mooncake.component.MooncakeCrust;
import com.mooncake.component.MooncakeFillings;
import com.mooncake.component.MooncakeInscription;
import com.mooncake.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MooncakeBlockEntity extends BlockEntity {
    private MooncakeFillings fillings = MooncakeFillings.EMPTY;
    private MooncakeCrust crust = MooncakeCrust.EMPTY;
    private MooncakeInscription inscription = MooncakeInscription.EMPTY;

    public MooncakeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MOONCAKE.get(), pos, state);
    }

    public MooncakeFillings getFillings() {
        return fillings;
    }

    public void setFillings(MooncakeFillings fillings) {
        this.fillings = fillings == null ? MooncakeFillings.EMPTY : fillings;
        setChanged();
        syncToClients();
    }

    public MooncakeCrust getCrust() {
        return crust;
    }

    public void setCrust(MooncakeCrust crust) {
        this.crust = crust == null ? MooncakeCrust.EMPTY : crust;
        setChanged();
        syncToClients();
    }

    public MooncakeInscription getInscription() {
        return inscription;
    }

    public void setInscription(MooncakeInscription inscription) {
        this.inscription = inscription == null ? MooncakeInscription.EMPTY : inscription;
        setChanged();
        syncToClients();
    }

    public void setFromStack(ItemStack stack) {
        this.fillings = MooncakeFillings.get(stack);
        this.crust = MooncakeCrust.get(stack);
        this.inscription = MooncakeInscription.get(stack);
        setChanged();
        syncToClients();
    }

    public void writeToStack(ItemStack stack) {
        MooncakeFillings.set(stack, fillings);
        MooncakeCrust.set(stack, crust);
        MooncakeInscription.set(stack, inscription);
    }

    public void syncToClients() {
        if (level instanceof ServerLevel serverLevel) {
            ClientboundBlockEntityDataPacket packet = ClientboundBlockEntityDataPacket.create(this);
            serverLevel.getServer().getPlayerList().broadcast(
                    null,
                    worldPosition.getX(),
                    worldPosition.getY(),
                    worldPosition.getZ(),
                    64.0D,
                    serverLevel.dimension(),
                    packet
            );
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        var ops = registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
        if (!fillings.isEmpty()) {
            tag.put("Fillings", MooncakeFillings.CODEC.encodeStart(ops, fillings).getOrThrow());
        }
        if (!crust.isEmpty()) {
            tag.put("Crust", MooncakeCrust.CODEC.encodeStart(ops, crust).getOrThrow());
        }
        if (!inscription.isEmpty()) {
            tag.putString("Inscription", inscription.text());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        var ops = registries.createSerializationContext(net.minecraft.nbt.NbtOps.INSTANCE);
        if (tag.contains("Fillings")) {
            fillings = MooncakeFillings.CODEC.parse(ops, tag.get("Fillings")).result().orElse(MooncakeFillings.EMPTY);
        } else {
            fillings = MooncakeFillings.EMPTY;
        }
        if (tag.contains("Crust")) {
            crust = MooncakeCrust.CODEC.parse(ops, tag.get("Crust")).result().orElse(MooncakeCrust.EMPTY);
        } else {
            crust = MooncakeCrust.EMPTY;
        }
        if (tag.contains("Inscription")) {
            inscription = MooncakeInscription.of(tag.getString("Inscription"));
        } else {
            inscription = MooncakeInscription.EMPTY;
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