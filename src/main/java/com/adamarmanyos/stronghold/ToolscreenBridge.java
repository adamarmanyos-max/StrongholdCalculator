package com.adamarmanyos.stronghold;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.util.math.MatrixStack;

/**
 * Talks to Toolscreen Mobile's {@code ToolscreenApi} when it is installed.
 *
 * <p>By reflection, so there is no build dependency between the two mods and
 * this one works the same without it. Every call is a no-op returning false
 * when Toolscreen Mobile is absent or too old.
 */
public final class ToolscreenBridge {
    private static final String MOD_ID = "toolscreen-mobile";
    private static final String API_CLASS = "dev.toolscreen.mobile.api.ToolscreenApi";
    private static boolean resolved;
    private static Method isOverrideActive;
    private static Method isEyeMeasureActive;
    private static Method setEyeMeasure;
    private static Method registerSidePanel;
    private static boolean sidePanelRegistered;

    private ToolscreenBridge() {
    }

    private static synchronized void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;
        if (!FabricLoader.getInstance().isModLoaded(MOD_ID)) {
            return;
        }
        try {
            Class<?> api = Class.forName(API_CLASS);
            isOverrideActive = api.getMethod("isOverrideActive");
            isEyeMeasureActive = api.getMethod("isEyeMeasureActive");
            setEyeMeasure = api.getMethod("setEyeMeasure", boolean.class);
            registerSidePanel = api.getMethod("registerSidePanel", BiConsumer.class);
            System.out.println("[strongholdfinder] found Toolscreen Mobile, using its Eye Measure mode");
        }
        catch (ReflectiveOperationException | LinkageError e) {
            System.out.println("[strongholdfinder] Toolscreen Mobile is installed but has no ToolscreenApi (needs 0.2.0 or later)");
            isOverrideActive = null;
            isEyeMeasureActive = null;
            setEyeMeasure = null;
            registerSidePanel = null;
        }
    }

    public static boolean present() {
        ToolscreenBridge.resolve();
        return setEyeMeasure != null;
    }

    /** True while Toolscreen Mobile is showing any non-native mode. */
    public static boolean isOverrideActive() {
        return ToolscreenBridge.call(isOverrideActive);
    }

    public static boolean isEyeMeasureActive() {
        return ToolscreenBridge.call(isEyeMeasureActive);
    }

    public static boolean setEyeMeasure(boolean on) {
        return ToolscreenBridge.call(setEyeMeasure, on);
    }

    /** True once our panel draws in Toolscreen's letterbox instead of the HUD. */
    public static boolean sidePanelRegistered() {
        return sidePanelRegistered;
    }

    public static void registerSidePanel(BiConsumer<MatrixStack, int[]> renderer) {
        if (sidePanelRegistered) {
            return;
        }
        ToolscreenBridge.resolve();
        if (registerSidePanel == null) {
            return;
        }
        try {
            registerSidePanel.invoke(null, renderer);
            sidePanelRegistered = true;
        }
        catch (ReflectiveOperationException | RuntimeException e) {
            System.out.println("[strongholdfinder] could not register with Toolscreen Mobile: " + e);
        }
    }

    private static boolean call(Method method, Object... args) {
        ToolscreenBridge.resolve();
        if (method == null) {
            return false;
        }
        try {
            return Boolean.TRUE.equals(method.invoke(null, args));
        }
        catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }
}
