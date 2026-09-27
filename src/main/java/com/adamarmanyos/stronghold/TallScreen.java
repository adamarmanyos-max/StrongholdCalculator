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

    /**
     * With Toolscreen Mobile installed, switches its Eye Measure mode instead.
     * On iOS this is the only thing that works: Amethyst's glfwSetWindowSize
     * only records the numbers and never resizes anything.
     */
    public static void toggle(MinecraftClient client) {
        if (ToolscreenBridge.present()) {
            boolean on = !ToolscreenBridge.isEyeMeasureActive();
            if (ToolscreenBridge.setEyeMeasure(on)) {
                StrongholdOverlay.notice(on ? "Eye Measure on" : "Eye Measure off");
            } else {
                StrongholdOverlay.notice("Toolscreen has no Eye Measure mode");
            }
            return;
        }
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

