package com.mooncake.client;

import net.minecraft.client.gui.screens.Screen;

public final class ClientShiftAccess {
    private ClientShiftAccess() {
    }

    public static boolean isShiftDown() {
        return Screen.hasShiftDown();
    }
}
