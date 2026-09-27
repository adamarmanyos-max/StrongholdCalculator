package com.adamarmanyos.stronghold.calc;

public final class StrongholdRings {
    public static final int SNAPPING_RADIUS = 7;
    public static final int DISTANCE_PARAM = 32;
    public static final int RING_COUNT = 8;
    public static final int[] STRONGHOLDS_IN_RING = new int[]{3, 6, 10, 15, 21, 28, 36, 9};
    public static final int EYE_TARGET_IN_CHUNK = 8;
    public static final int STAIRCASE_IN_CHUNK = 4;
    public static final int MAX_CHUNK = 1527;
    private static final int DELTA_R = 1;
    private static double[] density;
    private static double[] cumulativeRadial;

    private StrongholdRings() {
    }

    public static double innerRadius(int ring) {
        return 32.0 * ((double)(4 + ring * 6) - 1.25);
    }

    public static double outerRadius(int ring) {
        return 32.0 * ((double)(4 + ring * 6) + 1.25);
    }

    public static double innerRadiusPostSnapping(int ring) {
        return StrongholdRings.innerRadius(ring) - 8.0 * Math.sqrt(2.0);
    }

    public static double outerRadiusPostSnapping(int ring) {
        return StrongholdRings.outerRadius(ring) + 8.0 * Math.sqrt(2.0);
    }

    public static int ringAt(double chunkRadius) {
        for (int ring = 0; ring < 8; ++ring) {
            if (!(chunkRadius >= StrongholdRings.innerRadiusPostSnapping(ring)) || !(chunkRadius <= StrongholdRings.outerRadiusPostSnapping(ring))) continue;
            return ring;
        }
        return -1;
    }

    public static int[] offsetWeights() {
        int[] weights = new int[15];
        for (int i = -26; i <= 30; ++i) {
            int offset = -(i >> 2);
            if (offset < -7 || offset > 7) continue;
            int n = offset + 7;
            weights[n] = weights[n] + 1;
        }
        return weights;
    }

    public static double maxDistance(double x, double z) {
        double r = Math.sqrt(x * x + z * z) / 16.0;
        double maxDistance = Double.POSITIVE_INFINITY;
        for (int ring = 0; ring < 8; ++ring) {
            double b;
            double inner = StrongholdRings.innerRadius(ring);
            double outer = StrongholdRings.outerRadius(ring);
            double angle = Math.PI / (double)STRONGHOLDS_IN_RING[ring];
            double a = inner * inner + r * r - 2.0 * r * inner * Math.cos(angle);
            double max = Math.sqrt(a > (b = outer * outer + r * r - 2.0 * r * outer * Math.cos(angle)) ? a : b);
            if (!(max < maxDistance)) continue;
            maxDistance = max;
        }
        return (maxDistance + Math.sqrt(2.0) * 7.5) * 16.0;
    }

    public static synchronized double density(double chunkX, double chunkZ) {
        StrongholdRings.init();
        double radius = Math.sqrt(chunkX * chunkX + chunkZ * chunkZ) / 1.0;
        int low = (int)radius;
        int high = low + 1;
        double t = radius - (double)low;
        if (high >= density.length) {
            return 0.0;
        }
        return (1.0 - t) * density[low] + t * density[high];
    }

    public static synchronized double cumulativePolar(double radius) {
        if (radius < 0.0) {
            return 0.0;
        }
        StrongholdRings.init();
        double k = radius / 1.0;
        int low = (int)k;
        int high = low + 1;
        double t = k - (double)low;
        if (high >= cumulativeRadial.length) {
            return cumulativeRadial[cumulativeRadial.length - 1];
        }
        return (1.0 - t) * cumulativeRadial[low] + t * cumulativeRadial[high];
    }

    private static void init() {
        if (density != null) {
            return;
        }
        int size = 1532;
        double[] preSnapping = new double[size];
        for (int ring = 0; ring < 8; ++ring) {
            int c0 = (int)StrongholdRings.innerRadius(ring);
            int c1 = (int)StrongholdRings.outerRadius(ring);
            for (int i = c0; i <= c1; ++i) {
                double rho = (double)STRONGHOLDS_IN_RING[ring] / (Math.PI * 2 * (StrongholdRings.outerRadius(ring) - StrongholdRings.innerRadius(ring)) * (double)i);
                if (i == c0 || i == c1) {
                    rho *= 0.5;
                }
                if (i / 1 >= size) continue;
                preSnapping[i / 1] = rho;
            }
        }
        double[] filter = new double[(int)Math.ceil(7.0 * Math.sqrt(2.0)) + 1];
        double sum = 0.0;
        int[] offsets = StrongholdRings.offsetWeights();
        for (int k = -7; k <= 7; ++k) {
            int xWeight = offsets[k + 7];
            for (int l = -7; l <= 7; ++l) {
                int zWeight = offsets[l + 7];
                int w = xWeight * zWeight;
                int n = 200;
                for (int i = 0; i < n; ++i) {
                    double phi = Math.PI * 2 * (double)i / (double)n;
                    int dr = Math.abs(Math.round((float)(Math.sqrt(k * k + l * l) * Math.sin(phi))));
                    if (dr >= filter.length) continue;
                    int n2 = dr;
                    filter[n2] = filter[n2] + (double)w;
                    sum += dr == 0 ? (double)w : (double)(2 * w);
                }
            }
        }
        int i = 0;
        while (i < filter.length) {
            int n = i++;
            filter[n] = filter[n] / sum;
        }
        double[] smoothed = new double[size];
        for (int i2 = 0; i2 < size; ++i2) {
            for (int j = -filter.length + 1; j < filter.length; ++j) {
                if (i2 + j < 0 || i2 + j >= size) continue;
                int n = i2;
                smoothed[n] = smoothed[n] + preSnapping[i2 + j] * filter[j < 0 ? -j : j];
            }
        }
        double[] cumulative = new double[size];
        double running = 0.0;
        for (int i3 = 0; i3 < size; ++i3) {
            cumulative[i3] = running += smoothed[i3] * (double)i3 * 1.0 * 2.0 * Math.PI;
        }
        density = smoothed;
        cumulativeRadial = cumulative;
    }
}

