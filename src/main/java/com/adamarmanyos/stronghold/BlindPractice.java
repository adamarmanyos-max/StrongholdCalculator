package com.adamarmanyos.stronghold;

import com.adamarmanyos.stronghold.StrongholdOverlay;
import java.util.Random;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

public final class BlindPractice {
    private static final int EYE_COUNT = 1;
    private static final int PEARL_COUNT = 1;
    private static final double MIN_RADIUS = 800.0;
    private static final double MAX_RADIUS = 2200.0;
    private static final int MAX_ATTEMPTS = 8;
    private static final long SETTLE_MILLIS = 1500L;
    private static final Random RANDOM = new Random();
    private static boolean waiting;
    private static long checkAt;
    private static int attempts;

    private BlindPractice() {
    }

    public static void start(MinecraftClient client) {
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return;
        }
        attempts = 0;
        player.sendChatMessage("/gamemode creative");
        player.sendChatMessage("/give @s minecraft:ender_eye 1");
        player.sendChatMessage("/give @s minecraft:ender_pearl 1");
        BlindPractice.drop(player);
    }

    private static void drop(ClientPlayerEntity player) {
        ++attempts;
        double angle = RANDOM.nextDouble() * 2.0 * Math.PI;
        double radius = 800.0 + RANDOM.nextDouble() * 1400.0;
        long x = Math.round(radius * Math.cos(angle));
        long z = Math.round(radius * Math.sin(angle));
        player.sendChatMessage("/spreadplayers " + x + " " + z + " 0 8 false @s");
        waiting = true;
        checkAt = System.currentTimeMillis() + 1500L;
    }

    public static void tick(MinecraftClient client) {
        if (!waiting || System.currentTimeMillis() < checkAt) {
            return;
        }
        ClientPlayerEntity player = client.player;
        if (player == null) {
            waiting = false;
            return;
        }
        if (player.isTouchingWater()) {
            if (attempts < 8) {
                StrongholdOverlay.notice("Landed in water, moving...");
                BlindPractice.drop(player);
                return;
            }
            StrongholdOverlay.notice("Gave up avoiding water after " + attempts + " tries");
            waiting = false;
            return;
        }
        waiting = false;
        StrongholdOverlay.notice("Blind practice: " + Math.round(player.getX()) + ", " + Math.round(player.getZ()));
    }
}

