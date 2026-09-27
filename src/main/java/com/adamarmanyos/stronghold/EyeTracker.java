package com.adamarmanyos.stronghold;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public final class EyeTracker {
    private static final double SEARCH_RADIUS = 72.0;
    private static final double MIN_TRAVEL = 3.0;
    private static final double READY_TRAVEL = 10.0;
    public static final double TRACKED_SIGMA = 0.005;
    private static int trackedEntityId = -1;
    private static double originX;
    private static double originZ;
    private static double latestX;
    private static double latestZ;
    private static boolean tracking;
    private static boolean captured;

    public static boolean isReady() {
        return tracking && !captured && EyeTracker.travelled() >= 10.0;
    }

    private EyeTracker() {
    }

    public static void tick(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null || client.world == null) {
            return;
        }
        EyeOfEnderEntity chosen = null;
        EyeOfEnderEntity nearestEye = null;
        double nearestDistance = 5184.0;
        for (Entity entity : client.world.getEntities()) {
            if (!(entity instanceof EyeOfEnderEntity)) continue;
            EyeOfEnderEntity eye = (EyeOfEnderEntity)entity;
            if (eye.getEntityId() == trackedEntityId) {
                chosen = eye;
                break;
            }
            double distance = eye.squaredDistanceTo(player);
            if (!(distance < nearestDistance)) continue;
            nearestDistance = distance;
            nearestEye = eye;
        }
        if (chosen == null) {
            if (nearestEye == null) {
                return;
            }
            chosen = nearestEye;
            trackedEntityId = chosen.getEntityId();
            originX = chosen.getX();
            originZ = chosen.getZ();
            tracking = true;
            captured = false;
        }
        latestX = chosen.getX();
        latestZ = chosen.getZ();
    }

    public static boolean hasBearing() {
        return tracking && EyeTracker.travelled() >= 3.0;
    }

    public static double travelled() {
        return Math.hypot(latestX - originX, latestZ - originZ);
    }

    public static double bearingDegrees() {
        return Math.toDegrees(Math.atan2(-(latestX - originX), latestZ - originZ));
    }

    public static double originX() {
        return originX;
    }

    public static double originZ() {
        return originZ;
    }

    public static void forget() {
        trackedEntityId = -1;
        tracking = false;
        captured = false;
    }

    public static boolean isCaptured() {
        return captured;
    }

    public static void markCaptured() {
        captured = true;
    }
}

