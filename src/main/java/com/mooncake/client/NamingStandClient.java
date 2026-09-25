package com.mooncake.client;

import com.mooncake.network.MooncakeNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public final class NamingStandClient {
    private NamingStandClient() {
    }

    public static void open(BlockPos pos, String currentText) {
        Minecraft.getInstance().setScreen(new NamingStandScreen(pos, currentText));
    }

    private static final class NamingStandScreen extends Screen {
        private final BlockPos pos;
        private final String initial;
        private EditBox nameBox;

        private NamingStandScreen(BlockPos pos, String initial) {
            super(Component.translatable("gui.mooncake.naming_stand"));
            this.pos = pos;
            this.initial = initial == null ? "" : initial;
        }

        @Override
        protected void init() {
            int cx = this.width / 2;
            int cy = this.height / 2;
            nameBox = new EditBox(this.font, cx - 100, cy - 10, 200, 20, Component.translatable("gui.mooncake.naming_stand.name"));
            nameBox.setMaxLength(24);
            nameBox.setValue(initial);
            nameBox.setResponder(s -> {});
            addRenderableWidget(nameBox);
            setInitialFocus(nameBox);

            addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> saveAndClose())
                    .bounds(cx - 100, cy + 20, 95, 20)
                    .build());
            addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), b -> onClose())
                    .bounds(cx + 5, cy + 20, 95, 20)
                    .build());
        }

        private void saveAndClose() {
            PacketDistributor.sendToServer(new MooncakeNetwork.SetNamingStandTextPayload(pos, nameBox.getValue()));
            onClose();
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            renderBackground(graphics, mouseX, mouseY, partialTick);
            super.render(graphics, mouseX, mouseY, partialTick);
            graphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 2 - 40, 0xFFFFFF);
            graphics.drawCenteredString(
                    this.font,
                    Component.translatable("gui.mooncake.naming_stand.hint"),
                    this.width / 2,
                    this.height / 2 - 28,
                    0xA0A0A0
            );
        }

        @Override
        public boolean isPauseScreen() {
            return false;
        }
    }
}
