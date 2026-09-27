package com.adamarmanyos.stronghold.calc;

public final class StrongholdPrediction {
    /** Where eyes of ender point in 1.16: block (8, 8) of the stronghold chunk. */
    public static final int EYE_TARGET_IN_CHUNK = 8;
    /** Where the starter staircase is: block (4, 4) of the chunk. Shown to the player. */
    public static final int STAIRCASE_IN_CHUNK = 4;

    public final int chunkX;
    public final int chunkZ;
    public final int overworldX;
    public final int overworldZ;
    public final double certainty;

    public StrongholdPrediction(int chunkX, int chunkZ, double certainty) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.overworldX = chunkX * 16 + STAIRCASE_IN_CHUNK;
        this.overworldZ = chunkZ * 16 + STAIRCASE_IN_CHUNK;
        this.certainty = certainty;
    }

    /**
     * The point an eye would fly towards. Angle errors have to be measured
     * against this, not the staircase: the four blocks between them are up to
     * 0.2 degrees at 1500 blocks, which is 200 times a boat-eye aim error and
     * made every precise throw look wrong.
     */
    public double targetX() {
        return this.chunkX * 16 + EYE_TARGET_IN_CHUNK;
    }

    public double targetZ() {
        return this.chunkZ * 16 + EYE_TARGET_IN_CHUNK;
    }

    public boolean hasCertainty() {
        return !Double.isNaN(this.certainty);
    }

    public int netherX() {
        return Math.floorDiv(this.overworldX, 8);
    }

    public int netherZ() {
        return Math.floorDiv(this.overworldZ, 8);
    }
}
