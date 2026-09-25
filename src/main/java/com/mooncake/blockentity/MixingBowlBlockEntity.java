package com.mooncake.blockentity;

import com.mooncake.component.MooncakeCrust;
import com.mooncake.config.MooncakeConfig;
import com.mooncake.registry.ModBlockEntities;
import com.mooncake.registry.ModItems;
import com.mooncake.util.CrustHelper;
import com.mooncake.util.MooncakeSeriesGuard;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Two-step dough mixing:
 * <ol>
 *   <li>wheat×2 + egg + sugar → incomplete mooncake dough</li>
 *   <li>incomplete dough + optional crust extras → finished mooncake dough</li>
 * </ol>
 */
public class MixingBowlBlockEntity extends BlockEntity {
    public static final int REQUIRED_WHEAT = 2;
    public static final int REQUIRED_EGG = 1;
    public static final int REQUIRED_SUGAR = 1;
    public static final int OUTPUT_COUNT = 2;
    public static final int MAX_INCOMPLETE = 16;

    private int wheat;
    private int egg;
    private int sugar;
    private int incomplete;
    private final List<ItemStack> extras = new ArrayList<>();
    private ItemStack output = ItemStack.EMPTY;
    private int mixProgress;
    private boolean mixing;

    public MixingBowlBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MIXING_BOWL.get(), pos, state);
    }

    public int getWheat() {
        return wheat;
    }

    public int getEgg() {
        return egg;
    }

    public int getSugar() {
        return sugar;
    }

    public int getIncomplete() {
        return incomplete;
    }

    public List<ItemStack> getExtras() {
        return List.copyOf(extras);
    }

    private int extrasTotalCount() {
        int count = 0;
        for (ItemStack stack : extras) {
            count += Math.max(1, stack.getCount());
        }
        return count;
    }

    public ItemStack getOutput() {
        return output;
    }

    /** Items shown above the bowl: one rendered item per actual count. */
    public List<ItemStack> getDisplayItems() {
        List<ItemStack> items = new ArrayList<>();
        if (!output.isEmpty()) {
            appendByCount(items, output);
            return items;
        }
        for (int i = 0; i < wheat; i++) {
            items.add(new ItemStack(Items.WHEAT));
        }
        for (int i = 0; i < egg; i++) {
            items.add(new ItemStack(Items.EGG));
        }
        for (int i = 0; i < sugar; i++) {
            items.add(new ItemStack(Items.SUGAR));
        }
        for (int i = 0; i < incomplete; i++) {
            items.add(new ItemStack(ModItems.INCOMPLETE_MOONCAKE_DOUGH.get()));
        }
        for (ItemStack extra : extras) {
            appendByCount(items, extra);
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

    public int getMixProgress() {
        return mixProgress;
    }

    public int getMixTime() {
        return Math.max(1, MooncakeConfig.mixingBowlTime());
    }

    public boolean isMixing() {
        return mixing;
    }

    public boolean isFinishingStage() {
        return incomplete > 0;
    }

    public boolean canStartBaseMix() {
        return !mixing
                && output.isEmpty()
                && incomplete == 0
                && extras.isEmpty()
                && wheat >= REQUIRED_WHEAT
                && egg >= REQUIRED_EGG
                && sugar >= REQUIRED_SUGAR;
    }

    public boolean canStartFinishMix() {
        return !mixing
                && output.isEmpty()
                && incomplete > 0
                && wheat == 0
                && egg == 0
                && sugar == 0;
    }

    public boolean canStartMixing() {
        return canStartBaseMix() || canStartFinishMix();
    }

    public boolean tryStartMixing() {
        if (!canStartMixing()) {
            return false;
        }
        mixing = true;
        mixProgress = 0;
        setChangedAndSync();
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.SLIME_BLOCK_HIT, SoundSource.BLOCKS, 0.4F, 1.2F);
        }
        return true;
    }

    public boolean tryInsert(ItemStack stack, Player player) {
        if (mixing || stack.isEmpty()) {
            return false;
        }
        // Finished product sits on the bowl; start a new batch by handing it to the player first.
        if (!output.isEmpty()) {
            giveOrDrop(player, output);
            output = ItemStack.EMPTY;
            playExtract();
            setChangedAndSync();
        }

        // Stage 2: incomplete dough + optional extras
        if (incomplete > 0 || stack.is(ModItems.INCOMPLETE_MOONCAKE_DOUGH.get())) {
            if (stack.is(ModItems.INCOMPLETE_MOONCAKE_DOUGH.get()) && incomplete < MAX_INCOMPLETE
                    && wheat == 0 && egg == 0 && sugar == 0) {
                incomplete++;
                consumeOne(stack, player);
                playInsert();
                setChangedAndSync();
                return true;
            }
            ItemStack one = stack.copyWithCount(1);
            if (incomplete > 0 && wheat == 0 && egg == 0 && sugar == 0) {
                if (MooncakeSeriesGuard.isBlockedAsIngredient(one)) {
                    MooncakeSeriesGuard.warnPlayer(player);
                    return false;
                }
                if (CrustHelper.isValidCrust(one) && extrasTotalCount() < MooncakeConfig.maxCrusts()) {
                    boolean merged = false;
                    for (ItemStack existing : extras) {
                        if (ItemStack.isSameItemSameComponents(existing, one)) {
                            existing.setCount(existing.getCount() + 1);
                            merged = true;
                            break;
                        }
                    }
                    if (!merged) {
                        extras.add(one);
                    }
                    consumeOne(stack, player);
                    playInsert();
                    setChangedAndSync();
                    return true;
                }
            }
            return false;
        }

        // Stage 1: wheat / egg / sugar only (no extras yet)
        if (stack.is(Items.WHEAT) && wheat < REQUIRED_WHEAT) {
            wheat++;
            consumeOne(stack, player);
            playInsert();
            setChangedAndSync();
            return true;
        }
        if (stack.is(Items.EGG) && egg < REQUIRED_EGG) {
            egg++;
            consumeOne(stack, player);
            playInsert();
            setChangedAndSync();
            return true;
        }
        if (stack.is(Items.SUGAR) && sugar < REQUIRED_SUGAR) {
            sugar++;
            consumeOne(stack, player);
            playInsert();
            setChangedAndSync();
            return true;
        }
        return false;
    }

    public boolean tryExtract(Player player) {
        if (mixing) {
            return false;
        }
        if (!output.isEmpty()) {
            giveOrDrop(player, output);
            output = ItemStack.EMPTY;
            setChangedAndSync();
            playExtract();
            return true;
        }
        if (!extras.isEmpty()) {
            ItemStack last = extras.get(extras.size() - 1);
            giveOrDrop(player, last.copyWithCount(1));
            if (last.getCount() > 1) {
                last.shrink(1);
            } else {
                extras.remove(extras.size() - 1);
            }
            setChangedAndSync();
            playExtract();
            return true;
        }
        if (incomplete > 0) {
            incomplete--;
            giveOrDrop(player, new ItemStack(ModItems.INCOMPLETE_MOONCAKE_DOUGH.get()));
            setChangedAndSync();
            playExtract();
            return true;
        }
        if (sugar > 0) {
            sugar--;
            giveOrDrop(player, new ItemStack(Items.SUGAR));
            setChangedAndSync();
            playExtract();
            return true;
        }
        if (egg > 0) {
            egg--;
            giveOrDrop(player, new ItemStack(Items.EGG));
            setChangedAndSync();
            playExtract();
            return true;
        }
        if (wheat > 0) {
            wheat--;
            giveOrDrop(player, new ItemStack(Items.WHEAT));
            setChangedAndSync();
            playExtract();
            return true;
        }
        return false;
    }

    public List<ItemStack> getDropStacks() {
        List<ItemStack> drops = new ArrayList<>();
        if (wheat > 0) {
            drops.add(new ItemStack(Items.WHEAT, wheat));
        }
        if (egg > 0) {
            drops.add(new ItemStack(Items.EGG, egg));
        }
        if (sugar > 0) {
            drops.add(new ItemStack(Items.SUGAR, sugar));
        }
        if (incomplete > 0) {
            drops.add(new ItemStack(ModItems.INCOMPLETE_MOONCAKE_DOUGH.get(), incomplete));
        }
        for (ItemStack extra : extras) {
            drops.add(extra.copy());
        }
        if (!output.isEmpty()) {
            drops.add(output.copy());
        }
        return drops;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MixingBowlBlockEntity bowl) {
        if (!bowl.mixing) {
            return;
        }
        bowl.mixProgress++;
        if (bowl.mixProgress % 10 == 0) {
            level.playSound(null, pos, SoundEvents.SLIME_SQUISH_SMALL, SoundSource.BLOCKS, 0.15F, 1.4F);
            if (level instanceof ServerLevel serverLevel) {
                ItemStack particle = bowl.isFinishingStage()
                        ? new ItemStack(ModItems.INCOMPLETE_MOONCAKE_DOUGH.get())
                        : new ItemStack(Items.WHEAT);
                serverLevel.sendParticles(
                        new ItemParticleOption(ParticleTypes.ITEM, particle),
                        pos.getX() + 0.5D,
                        pos.getY() + 0.35D,
                        pos.getZ() + 0.5D,
                        3,
                        0.15D,
                        0.05D,
                        0.15D,
                        0.02D
                );
            }
        }
        if (bowl.mixProgress >= bowl.getMixTime()) {
            bowl.finishMixing();
        } else if (bowl.mixProgress % 5 == 0) {
            bowl.setChangedAndSync();
        }
    }

    private void finishMixing() {
        if (incomplete > 0) {
            MooncakeCrust crust = MooncakeCrust.of(extras);
            ItemStack dough = new ItemStack(ModItems.MOONCAKE_DOUGH.get(), incomplete);
            MooncakeCrust.set(dough, crust);
            incomplete = 0;
            extras.clear();
            output = dough;
        } else {
            output = new ItemStack(ModItems.INCOMPLETE_MOONCAKE_DOUGH.get(), OUTPUT_COUNT);
            wheat = 0;
            egg = 0;
            sugar = 0;
            extras.clear();
        }
        mixing = false;
        mixProgress = 0;
        setChangedAndSync();
        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.6F, 1.0F);
        }
    }

    private static void consumeOne(ItemStack stack, Player player) {
        if (!player.hasInfiniteMaterials()) {
            stack.shrink(1);
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
        tag.putInt("Wheat", wheat);
        tag.putInt("Egg", egg);
        tag.putInt("Sugar", sugar);
        tag.putInt("Incomplete", incomplete);
        tag.putInt("MixProgress", mixProgress);
        tag.putBoolean("Mixing", mixing);
        ListTag extrasTag = new ListTag();
        for (ItemStack stack : extras) {
            extrasTag.add(stack.save(registries));
        }
        tag.put("Extras", extrasTag);
        if (!output.isEmpty()) {
            tag.put("Output", output.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        wheat = tag.getInt("Wheat");
        egg = tag.getInt("Egg");
        sugar = tag.getInt("Sugar");
        incomplete = tag.getInt("Incomplete");
        mixProgress = tag.getInt("MixProgress");
        mixing = tag.getBoolean("Mixing");
        extras.clear();
        ListTag extrasTag = tag.getList("Extras", Tag.TAG_COMPOUND);
        for (int i = 0; i < extrasTag.size(); i++) {
            ItemStack.parse(registries, extrasTag.getCompound(i)).ifPresent(extras::add);
        }
        output = ItemStack.EMPTY;
        if (tag.contains("Output")) {
            output = ItemStack.parse(registries, tag.getCompound("Output")).orElse(ItemStack.EMPTY);
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
