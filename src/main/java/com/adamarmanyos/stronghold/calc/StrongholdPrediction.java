package com.adamarmanyos.stronghold.calc;

public final class StrongholdPrediction {
    public final int overworldX;
    public final int overworldZ;
    public final double errorRadius;
    public final double crossingAngle;
    public final double certainty;
    public double distanceFromThrow = Double.NaN;

    public StrongholdPrediction(int overworldX, int overworldZ, double errorRadius, double crossingAngle, double certainty) {
        this.overworldX = overworldX;
        this.overworldZ = overworldZ;
        this.errorRadius = errorRadius;
        this.crossingAngle = crossingAngle;
        this.certainty = certainty;
    }

    public boolean hasCertainty() {
        return !Double.isNaN(this.certainty);
    }

    public boolean hasUncertainty() {
        return !Double.isNaN(this.errorRadius);
    }

    public int netherX() {
        return Math.floorDiv(this.overworldX, 8);
    }

    public int netherZ() {
        return Math.floorDiv(this.overworldZ, 8);
    }
}

