package com.mooncake.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Create-belt style stacked items: flat on the surface, piled upward by count.
 */
public abstract class OrderedItemBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {
    /** Vertical spacing between stacked items (same feel as Create belt stacks). */
    private static final float STACK_STEP = 1.0F / 16.0F;
    private static final float ITEM_SCALE = 0.5F;

    private final ItemRenderer itemRenderer;
    private final float baseHeight;
    private final RandomSource random = RandomSource.create();

    protected OrderedItemBlockEntityRenderer(BlockEntityRendererProvider.Context context, float baseHeight) {
        this.itemRenderer = context.getItemRenderer();
        this.baseHeight = baseHeight;
    }

    protected abstract List<ItemStack> displayItems(T blockEntity);

    protected abstract boolean animate(T blockEntity);

    @Override
    public void render(
            T blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        List<ItemStack> items = displayItems(blockEntity);
        if (items.isEmpty()) {
            return;
        }
        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }

        boolean mixing = animate(blockEntity);
        float spin = mixing ? (level.getGameTime() + partialTick) * 8.0F : 0.0F;

        poseStack.pushPose();
        poseStack.translate(0.5D, baseHeight, 0.5D);
        if (mixing) {
            poseStack.mulPose(Axis.YP.rotationDegrees(spin));
        }

        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (stack.isEmpty()) {
                continue;
            }

            // Stable per-layer jitter so the pile looks like Create belt stacks.
            random.setSeed(blockEntity.getBlockPos().asLong() * 31L + i * 9973L + stack.getItem().hashCode());
            float jitterX = (random.nextFloat() - 0.5F) * 0.08F;
            float jitterZ = (random.nextFloat() - 0.5F) * 0.08F;
            float yaw = random.nextFloat() * 360.0F;

            poseStack.pushPose();
            poseStack.translate(jitterX, i * STACK_STEP, jitterZ);
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            // Flat on the surface, like items on a Create belt.
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);

            itemRenderer.renderStatic(
                    stack,
                    ItemDisplayContext.FIXED,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    buffer,
                    level,
                    i
            );
            poseStack.popPose();
        }
        poseStack.popPose();
    }
}
