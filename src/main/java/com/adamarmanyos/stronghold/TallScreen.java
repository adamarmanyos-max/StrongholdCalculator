package com.adamarmanyos.stronghold;

import com.adamarmanyos.stronghold.StrongholdConfig;
import com.adamarmanyos.stronghold.StrongholdOverlay;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public final class TallScreen {
    public static final int USEFUL_HEIGHT = 3071;
    private static boolean tall;
    private static int previousWidth;
    private static int previousHeight;

    private TallScreen() {
    }

    public static boolean isTall() {
        return tall;
    }

    public static void toggle(MinecraftClient client) {
        if (client.options.fullscreen) {
            StrongholdOverlay.notice("Leave fullscreen first");
            return;
        }
        long handle = client.getWindow().getHandle();
        if (tall) {
            GLFW.glfwSetWindowSize((long)handle, (int)previousWidth, (int)previousHeight);
            tall = false;
            StrongholdOverlay.notice("Screen restored");
            return;
        }
        previousWidth = client.getWindow().getWidth();
        previousHeight = client.getWindow().getHeight();
        GLFW.glfwSetWindowSize((long)handle, (int)StrongholdConfig.tallWidth(), (int)StrongholdConfig.tallHeight());
        tall = true;
        StrongholdOverlay.notice("Tall screen " + StrongholdConfig.tallWidth() + "x" + StrongholdConfig.tallHeight());
    }

    public static void restoreIfNeeded(MinecraftClient client) {
        if (!tall) {
            return;
        }
        GLFW.glfwSetWindowSize((long)client.getWindow().getHandle(), (int)previousWidth, (int)previousHeight);
        tall = false;
    }
}

