package com.mooncake.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mooncake.MooncakeMod;
import com.mooncake.block.MooncakeBlocks;
import com.mooncake.block.MooncakeFacing;
import com.mooncake.blockentity.MooncakeBlockEntity;
import com.mooncake.component.MooncakeInscription;
import com.mooncake.util.MooncakeInscriptionLayout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Whole cakes: Fangsong inscription in the mold frame.
 * Pieces: filling embeds on cut faces + crust texture scraps beside the slice.
 */
public class MooncakeInscriptionRenderer implements BlockEntityRenderer<MooncakeBlockEntity> {
    private static final ResourceLocation FANGSONG = ResourceLocation.fromNamespaceAndPath(MooncakeMod.MOD_ID, "fangsong");
    private static final int TEXT_COLOR = 0xFFE8D2A8;

    private final ItemRenderer itemRenderer;

    public MooncakeInscriptionRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(
            MooncakeBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        BlockState state = blockEntity.getBlockState();
        Block block = state.getBlock();
        if (MooncakeBlocks.isPiece(block)) {
            MooncakePieceDecor.render(blockEntity, itemRenderer, poseStack, buffer, packedLight, packedOverlay);
            return;
        }

        MooncakeInscription inscription = blockEntity.getInscription();
        if (inscription.isEmpty()) {
            return;
        }

        Direction facing = state.hasProperty(MooncakeFacing.FACING)
                ? state.getValue(MooncakeFacing.FACING)
                : Direction.SOUTH;

        float y = MooncakeInscriptionLayout.HEIGHT + MooncakeInscriptionLayout.PANEL_EXTRA;
        float x0 = MooncakeInscriptionLayout.PANEL_X0;
        float x1 = MooncakeInscriptionLayout.PANEL_X1;
        float z0 = MooncakeInscriptionLayout.PANEL_Z0;
        float z1 = MooncakeInscriptionLayout.PANEL_Z1;

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.0D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-MooncakeFacing.yRotation(facing)));
        poseStack.translate(-0.5D, 0.0D, -0.5D);

        Font font = Minecraft.getInstance().font;
        Style style = Style.EMPTY.withFont(FANGSONG);
        String text = inscription.text();
        int[] cps = text.codePoints().toArray();
        if (cps.length == 0) {
            poseStack.popPose();
            return;
        }

        float panelLong = x1 - x0;
        float panelShort = z1 - z0;
        float maxGlyphW = 1.0F;
        for (int cp : cps) {
            FormattedCharSequence seq =
                    Component.literal(new String(Character.toChars(cp))).withStyle(style).getVisualOrderText();
            maxGlyphW = Math.max(maxGlyphW, font.width(seq));
        }
        float lineH = Math.max(1.0F, font.lineHeight);
        final float inset = 0.78F;
        float scaleW = (panelShort * inset) / maxGlyphW;
        float scaleH = (panelLong * inset) / (lineH * cps.length);
        float scale = Math.min(scaleW, scaleH);
        scale = Math.min(scale, panelShort * inset / lineH);
        scale = Math.max(0.004F, scale);

        float step = lineH * scale;
        float totalLen = step * cps.length;
        float startX = ((x0 + x1) * 0.5F) - totalLen * 0.5F;
        float cz = (z0 + z1) * 0.5F;

        for (int i = 0; i < cps.length; i++) {
            int cpIndex = cps.length - 1 - i;
            String ch = new String(Character.toChars(cps[cpIndex]));
            FormattedCharSequence seq = Component.literal(ch).withStyle(style).getVisualOrderText();
            float gw = font.width(seq);
            poseStack.pushPose();
            float x = startX + (i + 0.5F) * step;
            poseStack.translate(x, y + 0.0015F, cz);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            poseStack.scale(scale, scale, scale);
            font.drawInBatch(
                    seq,
                    -gw * 0.5F,
                    -lineH * 0.5F,
                    TEXT_COLOR,
                    false,
                    poseStack.last().pose(),
                    buffer,
                    Font.DisplayMode.NORMAL,
                    0,
                    packedLight
            );
            poseStack.popPose();
        }
        poseStack.popPose();
    }
}
