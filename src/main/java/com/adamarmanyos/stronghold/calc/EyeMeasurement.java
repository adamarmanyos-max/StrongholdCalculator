package com.adamarmanyos.stronghold.calc;

public final class EyeMeasurement {
    public final double x;
    public final double z;
    public float yaw;
    public final double sigma;
    public final boolean fromEyeFlight;
    public boolean fromBoat;
    public float pitch;
    public int pixelAdjustments;

    public EyeMeasurement(double x, double z, float yaw, double sigma, boolean fromEyeFlight) {
        this.x = x;
        this.z = z;
        this.yaw = yaw;
        this.sigma = sigma;
        this.fromEyeFlight = fromEyeFlight;
    }

    public double[] direction() {
        double rad = Math.toRadians(this.yaw);
        return new double[]{-Math.sin(rad), Math.cos(rad)};
    }

    public String toString() {
        return "EyeMeasurement[x=" + this.x + ", z=" + this.z + ", yaw=" + this.yaw + "]";
    }
}

