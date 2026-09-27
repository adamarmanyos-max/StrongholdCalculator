package com.adamarmanyos.stronghold;

import com.adamarmanyos.stronghold.mixin.GameRendererAccessor;
import net.minecraft.client.MinecraftClient;

/**
 * Converts between screen pixels and degrees at the crosshair.
 *
 * <p>Both inputs are what is actually rendered, not what the settings say:
 * <ul>
 * <li>the framebuffer height, which Toolscreen Mobile's Eye Measure mode (and
 * {@link TallScreen}) make many times taller than the window. This used to read
 * the window height, which Toolscreen leaves alone, so every pixel adjustment
 * made in Eye Measure was several times too large;</li>
 * <li>the FOV from {@code GameRenderer.getFov}, which includes Toolscreen's
 * narrowing (half, by default) and the sprint multiplier. This used to assume
 * 30 degrees, as Ninjabrain Bot does.</li>
 * </ul>
 */
public final class PixelPerfect {
    /** A throw counts as boat eye only when one pixel is at most this many degrees. */
    public static final double MAX_BOAT_DEGREES_PER_PIXEL = 0.005;

    private PixelPerfect() {
    }

    /** Vertical FOV in degrees as rendered this frame, or the setting if that is unavailable. */
    public static double renderedFov(MinecraftClient client) {
        try {
            if (client.gameRenderer != null && client.gameRenderer.getCamera() != null) {
                return ((GameRendererAccessor)client.gameRenderer).strongholdfinder$getFov(client.gameRenderer.getCamera(), client.getTickDelta(), true);
            }
        }
        catch (RuntimeException ignored) {
            // Fall through to the setting.
        }
        return client.options.fov;
    }

    /** Degrees one framebuffer pixel covers at the screen centre, straight ahead. */
    public static double degreesPerPixel(MinecraftClient client) {
        int height = client.getWindow().getFramebufferHeight();
        double fov = renderedFov(client);
        if (height <= 0 || fov <= 0.0 || fov >= 180.0) {
            return 0.01;
        }
        return Math.toDegrees(Math.atan(2.0 * Math.tan(Math.toRadians(fov / 2.0)) / (double)height));
    }

    /**
     * Yaw change for a one pixel sideways shift at the crosshair, when looking
     * up or down by {@code pitchDegrees}. Same formula as Ninjabrain Bot's.
     */
    public static double yawDegreesPerPixel(MinecraftClient client, double pitchDegrees) {
        return degreesPerPixel(client) / Math.cos(Math.toRadians(pitchDegrees));
    }

    /** True when the view is magnified enough that a pixel count is worth boat-eye precision. */
    public static boolean zoomedForBoatEye(MinecraftClient client) {
        return degreesPerPixel(client) <= MAX_BOAT_DEGREES_PER_PIXEL;
    }

    /**
     * Correction for rounding in client-bound entity movement packets, which
     * biases where the eye appears to fly. Taken from Ninjabrain Bot.
     */
    public static double packetRoundingCorrection(double yawDegrees) {
        return 8.24E-4 * Math.sin((yawDegrees + 45.0) * Math.PI / 180.0);
    }

    public static boolean isOnCorner(double coordinate) {
        double fraction = coordinate - Math.floor(coordinate);
        return Math.abs(fraction - 0.3) < 1.0E-4 || Math.abs(fraction - 0.7) < 1.0E-4;
    }

    public static boolean isOnCorner(double x, double z) {
        return PixelPerfect.isOnCorner(x) && PixelPerfect.isOnCorner(z);
    }
}
