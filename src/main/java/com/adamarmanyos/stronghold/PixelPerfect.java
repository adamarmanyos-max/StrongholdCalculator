package com.adamarmanyos.stronghold;

import net.minecraft.client.MinecraftClient;

public final class PixelPerfect {
    private static final double HALF_FOV_DEGREES = 15.0;
    public static final double MEASURING_FOV = 30.0;
    public static final double SUBPIXEL_DEGREES = 0.01;

    private PixelPerfect() {
    }

    public static double degreesPerPixel(MinecraftClient client, double pitchDegrees) {
        int height = client.getWindow().getHeight();
        if (height <= 0) {
            return 0.01;
        }
        double toRadians = Math.PI / 180;
        double vertical = Math.atan(2.0 * Math.tan(15.0 * toRadians) / (double)height);
        double horizontal = vertical / Math.cos(pitchDegrees * toRadians);
        return horizontal / toRadians;
    }

    public static boolean atMeasuringFov(MinecraftClient client) {
        return Math.abs(client.options.fov - 30.0) < 0.5;
    }

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

