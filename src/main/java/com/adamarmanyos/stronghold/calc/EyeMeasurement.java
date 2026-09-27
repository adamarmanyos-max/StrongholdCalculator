package com.adamarmanyos.stronghold.calc;

public final class EyeMeasurement {
    public final double x;
    public final double z;
    public float yaw;
    public final double sigma;
    public final boolean fromEyeFlight;
    /** A boat-eye throw: measured zoomed in, trusted to the boat-eye aim error. */
    public boolean fromBoat;
    public float pitch;
    public int pixelAdjustments;
    /**
     * Yaw change for one pixel of adjustment, fixed when the throw is taken.
     * Leaving Eye Measure afterwards changes the pixel size on screen, but
     * the pixels being counted are the ones the throw was lined up in.
     */
    public double yawPerPixel;

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

