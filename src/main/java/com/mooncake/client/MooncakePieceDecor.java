package com.mooncake.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mooncake.block.MooncakeFacing;
import com.mooncake.block.MooncakeShapes;
import com.mooncake.blockentity.MooncakeBlockEntity;
import com.mooncake.component.MooncakeCrust;
import com.mooncake.component.MooncakeFillings;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix4f;

/**
 * Soft / hardened piece extras: filling item chunks embedded in cut faces,
 * plus crust-item texture scraps scattered beside the slice.
 * <p>
 * Positions are derived from {@link MooncakeShapes#pieceShape} so they stay
 * aligned with the collision box for every facing.
 */
final class MooncakePieceDecor {
    private static final float HEIGHT = 4.4F / 16.0F;
    /**
     * Offset from the cut plane along the outward normal.
     * Negative = into the cake; keep near 0 so chunks sit in the section without floating.
     */
    private static final float PROTRUDE_MIN = -0.02F;
    private static final float PROTRUDE_MAX = 0.025F;
    private static final float CENTER = 0.5F;
    /** Collision boxes can overhang the center line by up to ~0.5/16. */
    private static final float CUT_EPS = 0.1F;

    private MooncakePieceDecor() {
    }

    static void render(
            MooncakeBlockEntity blockEntity,
            ItemRenderer itemRenderer,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        Level level = blockEntity.getLevel();
        if (level == null) {
            return;
        }

        MooncakeFillings fillings = blockEntity.getFillings();
        MooncakeCrust crust = blockEntity.getCrust();
        List<ItemStack> fillingItems = expandStacks(fillings.items(), 8);
        List<ItemStack> crustItems = expandStacks(crust.items(), 8);
        if (fillingItems.isEmpty() && crustItems.isEmpty()) {
            return;
        }

        BlockState state = blockEntity.getBlockState();
        Direction facing = state.hasProperty(MooncakeFacing.FACING)
                ? state.getValue(MooncakeFacing.FACING)
                : Direction.EAST;

        VoxelShape shape = MooncakeShapes.pieceShape(facing);
        AABB box = shape.bounds();

        RandomSource random = RandomSource.create();
        random.setSeed(blockEntity.getBlockPos().asLong() * 341873128712L);

        if (!fillingItems.isEmpty()) {
            renderFillingEmbeds(fillingItems, itemRenderer, level, poseStack, buffer, packedLight, packedOverlay, random, box);
        }
        if (!crustItems.isEmpty()) {
            renderCrustScraps(crustItems, itemRenderer, level, poseStack, buffer, packedLight, random, box);
        }
    }

    private static void renderFillingEmbeds(
            List<ItemStack> items,
            ItemRenderer itemRenderer,
            Level level,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay,
            RandomSource random,
            AABB box
    ) {
        // Faces near the block center lines are the cut (section) faces.
        boolean cutAtMaxX = Math.abs(box.maxX - CENTER) < CUT_EPS;
        boolean cutAtMinX = Math.abs(box.minX - CENTER) < CUT_EPS;
        boolean cutAtMaxZ = Math.abs(box.maxZ - CENTER) < CUT_EPS;
        boolean cutAtMinZ = Math.abs(box.minZ - CENTER) < CUT_EPS;

        List<CutFace> faces = new ArrayList<>(2);
        if (cutAtMaxX) {
            faces.add(new CutFace(Direction.EAST, (float) Math.min(box.maxX, CENTER), (float) box.minZ, (float) box.maxZ));
        }
        if (cutAtMinX) {
            faces.add(new CutFace(Direction.WEST, (float) Math.max(box.minX, CENTER), (float) box.minZ, (float) box.maxZ));
        }
        if (cutAtMaxZ) {
            faces.add(new CutFace(Direction.SOUTH, (float) Math.min(box.maxZ, CENTER), (float) box.minX, (float) box.maxX));
        }
        if (cutAtMinZ) {
            faces.add(new CutFace(Direction.NORTH, (float) Math.max(box.minZ, CENTER), (float) box.minX, (float) box.maxX));
        }
        if (faces.isEmpty()) {
            return;
        }

        int count = Mth.clamp(2 + items.size(), 3, 7);
        float alongPad = 0.08F;
        for (int i = 0; i < count; i++) {
            ItemStack stack = items.get(i % items.size());
            if (stack.isEmpty()) {
                continue;
            }
            CutFace face = faces.get(i % faces.size());
            float span = face.along1 - face.along0 - alongPad * 2.0F;
            if (span <= 0.02F) {
                continue;
            }
            float along = face.along0 + alongPad + random.nextFloat() * span;
            float y = 0.08F + random.nextFloat() * (HEIGHT - 0.14F);
            // Mostly inside the cake; only a little peeks past the cut plane.
            float protrude = PROTRUDE_MIN + random.nextFloat() * (PROTRUDE_MAX - PROTRUDE_MIN);
            float scale = 0.18F + random.nextFloat() * 0.10F;
            // 东倒西歪，但幅度收一点以免整块甩出截面
            float yaw = random.nextFloat() * 360.0F;
            float pitch = (random.nextFloat() - 0.5F) * 45.0F;
            float roll = (random.nextFloat() - 0.5F) * 50.0F;

            poseStack.pushPose();
            switch (face.outward) {
                case EAST -> {
                    poseStack.translate(face.plane + protrude, y, along);
                    poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
                }
                case WEST -> {
                    poseStack.translate(face.plane - protrude, y, along);
                    poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
                }
                case SOUTH -> {
                    poseStack.translate(along, y, face.plane + protrude);
                }
                case NORTH -> {
                    poseStack.translate(along, y, face.plane - protrude);
                    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                }
                default -> {
                }
            }
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(pitch));
            poseStack.mulPose(Axis.ZP.rotationDegrees(roll));
            // Flatten along the cut normal so most of the item stays in the cake mass.
            poseStack.scale(scale, scale, scale * 0.4F);

            itemRenderer.renderStatic(
                    stack,
                    ItemDisplayContext.FIXED,
                    packedLight,
                    packedOverlay,
                    poseStack,
                    buffer,
                    level,
                    i + 11
            );
            poseStack.popPose();
        }
    }

    private static void renderCrustScraps(
            List<ItemStack> items,
            ItemRenderer itemRenderer,
            Level level,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            RandomSource random,
            AABB box
    ) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
        int scrapCount = Mth.clamp(2 + items.size(), 3, 8);

        for (int i = 0; i < scrapCount; i++) {
            ItemStack stack = items.get(i % items.size());
            if (stack.isEmpty()) {
                continue;
            }
            BakedModel model = itemRenderer.getModel(stack, level, null, i * 31);
            TextureAtlasSprite sprite = model.getParticleIcon();
            if (sprite == null || sprite.atlasLocation() == null) {
                continue;
            }

            float x = 0.08F + random.nextFloat() * 0.84F;
            float z = 0.08F + random.nextFloat() * 0.84F;
            // Keep scraps off the piece footprint (beside the slice).
            int guard = 0;
            while (guard++ < 12 && containsXZ(box, x, z, 0.04F)) {
                x = 0.08F + random.nextFloat() * 0.84F;
                z = 0.08F + random.nextFloat() * 0.84F;
            }
            if (containsXZ(box, x, z, 0.02F)) {
                // Nudge outward from piece center toward block edge.
                float cx = (float) ((box.minX + box.maxX) * 0.5);
                float cz = (float) ((box.minZ + box.maxZ) * 0.5);
                x = Mth.clamp(cx + Math.signum(cx - CENTER) * 0.35F, 0.08F, 0.92F);
                z = Mth.clamp(cz + Math.signum(cz - CENTER) * 0.35F, 0.08F, 0.92F);
            }

            float size = 0.06F + random.nextFloat() * 0.10F;
            float yaw = random.nextFloat() * 360.0F;
            float cropW = 0.25F + random.nextFloat() * 0.45F;
            float cropH = 0.25F + random.nextFloat() * 0.45F;
            float u0 = random.nextFloat() * (1.0F - cropW);
            float v0 = random.nextFloat() * (1.0F - cropH);

            poseStack.pushPose();
            poseStack.translate(x, 0.012F + random.nextFloat() * 0.01F, z);
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));

            blitCrop(poseStack.last(), consumer, sprite, -size * 0.5F, -size * 0.5F, size * 0.5F, size * 0.5F,
                    u0, v0, u0 + cropW, v0 + cropH, packedLight, true);
            blitCrop(poseStack.last(), consumer, sprite, -size * 0.5F, -size * 0.5F, size * 0.5F, size * 0.5F,
                    u0, v0, u0 + cropW, v0 + cropH, packedLight, false);
            poseStack.popPose();
        }
    }

    private static boolean containsXZ(AABB box, float x, float z, float inflate) {
        return x >= box.minX - inflate && x <= box.maxX + inflate
                && z >= box.minZ - inflate && z <= box.maxZ + inflate;
    }

    private static void blitCrop(
            PoseStack.Pose pose,
            VertexConsumer consumer,
            TextureAtlasSprite sprite,
            float x0,
            float y0,
            float x1,
            float y1,
            float u0,
            float v0,
            float u1,
            float v1,
            int light,
            boolean front
    ) {
        Matrix4f mat = pose.pose();
        float su0 = sprite.getU(u0);
        float su1 = sprite.getU(u1);
        float sv0 = sprite.getV(v0);
        float sv1 = sprite.getV(v1);
        int overlay = OverlayTexture.NO_OVERLAY;
        float nz = front ? 1.0F : -1.0F;

        if (front) {
            vertex(consumer, mat, pose, x0, y0, 0.0F, su0, sv1, light, overlay, nz);
            vertex(consumer, mat, pose, x1, y0, 0.0F, su1, sv1, light, overlay, nz);
            vertex(consumer, mat, pose, x1, y1, 0.0F, su1, sv0, light, overlay, nz);
            vertex(consumer, mat, pose, x0, y1, 0.0F, su0, sv0, light, overlay, nz);
        } else {
            vertex(consumer, mat, pose, x0, y1, 0.0F, su0, sv0, light, overlay, nz);
            vertex(consumer, mat, pose, x1, y1, 0.0F, su1, sv0, light, overlay, nz);
            vertex(consumer, mat, pose, x1, y0, 0.0F, su1, sv1, light, overlay, nz);
            vertex(consumer, mat, pose, x0, y0, 0.0F, su0, sv1, light, overlay, nz);
        }
    }

    private static void vertex(
            VertexConsumer consumer,
            Matrix4f mat,
            PoseStack.Pose pose,
            float x,
            float y,
            float z,
            float u,
            float v,
            int light,
            int overlay,
            float nz
    ) {
        consumer.addVertex(mat, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, 0.0F, 0.0F, nz);
    }

    private static List<ItemStack> expandStacks(List<ItemStack> source, int max) {
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack stack : source) {
            if (stack.isEmpty()) {
                continue;
            }
            int n = Math.min(stack.getCount(), Math.max(1, max - out.size()));
            for (int i = 0; i < n; i++) {
                out.add(stack.copyWithCount(1));
                if (out.size() >= max) {
                    return out;
                }
            }
        }
        return out;
    }

    /** One cut (section) face of the piece. */
    private record CutFace(Direction outward, float plane, float along0, float along1) {
    }
}
