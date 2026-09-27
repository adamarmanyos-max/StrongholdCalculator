package com.adamarmanyos.stronghold.calc;

import com.adamarmanyos.stronghold.calc.EyeMeasurement;
import com.adamarmanyos.stronghold.calc.StrongholdCalculator;
import com.adamarmanyos.stronghold.calc.StrongholdPrediction;
import com.adamarmanyos.stronghold.calc.StrongholdRings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class NinjabrainChunkCalculator
implements StrongholdCalculator {
    private static final double RANGE_CHUNKS = 312.5;
    private static final double MAX_TOLERANCE_DEGREES = 1.0;
    private static final double TOLERANCE_SIGMAS = 30.0;
    private static final int MAX_CANDIDATES = 3;
    private static final double MIN_REPORTED_CERTAINTY = 0.01;
    private static final double MAX_LATERAL_ERROR = 0.005 * Math.sqrt(2.0) * 180.0 / Math.PI;
    private static final int K = 7;
    private final boolean useClosestStrongholdCondition;
    private static final double TARGET_ERROR_BLOCKS = 4.0;
    private static final double TRAVEL_ANGLE_DEGREES = 30.0;

    public NinjabrainChunkCalculator() {
        this(true);
    }

    public NinjabrainChunkCalculator(boolean useClosestStrongholdCondition) {
        this.useClosestStrongholdCondition = useClosestStrongholdCondition;
    }

    @Override
    public String name() {
        return "Ninjabrain-style chunk posterior";
    }

    @Override
    public int minimumMeasurements() {
        return 1;
    }

    @Override
    public StrongholdCalculator.Result calculate(List<EyeMeasurement> measurements) {
        double tolerance;
        if (measurements.isEmpty()) {
            return StrongholdCalculator.Result.failure("Need an Eye measurement");
        }
        EyeMeasurement first = measurements.get(0);
        List<int[]> candidates = NinjabrainChunkCalculator.coneChunks(first, tolerance = Math.min(1.0, 30.0 * first.sigma));
        if (candidates.isEmpty()) {
            return StrongholdCalculator.Result.failure("No stronghold fits those angles");
        }
        double[] weights = new double[candidates.size()];
        double maxDistanceChunks = StrongholdRings.maxDistance(first.x, first.z) / 16.0;
        double maxDistanceSquared = maxDistanceChunks * maxDistanceChunks;
        for (int i = 0; i < candidates.size(); ++i) {
            double dz;
            int[] chunk = candidates.get(i);
            double dx = (double)chunk[0] - first.x / 16.0;
            if (dx * dx + (dz = (double)chunk[1] - first.z / 16.0) * dz > maxDistanceSquared) {
                weights[i] = 0.0;
                continue;
            }
            double weight = 0.0;
            for (int k = 0; k < 2; ++k) {
                for (int l = 0; l < 2; ++l) {
                    weight += StrongholdRings.density((double)chunk[0] - 0.5 + (double)k, (double)chunk[1] - 0.5 + (double)l);
                }
            }
            weights[i] = weight / 4.0;
        }
        for (int m = 0; m < measurements.size(); ++m) {
            NinjabrainChunkCalculator.condition(weights, candidates, measurements.get(m));
        }
        if (this.useClosestStrongholdCondition) {
            this.applyClosestStrongholdCondition(weights, candidates, first);
        }
        double total = 0.0;
        for (int i = 0; i < weights.length; ++i) {
            total += weights[i];
        }
        if (!(total > 0.0)) {
            return StrongholdCalculator.Result.failure("No stronghold fits those angles");
        }
        List<StrongholdPrediction> ranked = NinjabrainChunkCalculator.rank(weights, candidates);
        StrongholdCalculator.Result result = StrongholdCalculator.Result.of(ranked);
        result.advice = NinjabrainChunkCalculator.advice(weights, candidates, first);
        return result;
    }

    private static List<int[]> coneChunks(EyeMeasurement throwPoint, double toleranceDegrees) {
        double vk;
        boolean majorX;
        ArrayList<int[]> chunks = new ArrayList<int[]>();
        double phi = Math.toRadians(throwPoint.yaw);
        double tolerance = Math.toRadians(toleranceDegrees);
        double dx = -Math.sin(phi);
        double dz = Math.cos(phi);
        double ux = -Math.sin(phi - tolerance);
        double uz = Math.cos(phi - tolerance);
        double vx = -Math.sin(phi + tolerance);
        double vz = Math.cos(phi + tolerance);
        boolean bl = majorX = Math.cos(phi) * Math.cos(phi) < 0.5;
        boolean majorPositive = majorX ? dx > 0.0 : dz > 0.0;
        double originMajor = ((majorX ? throwPoint.x : throwPoint.z) - 8.0) / 16.0;
        double originMinor = ((majorX ? throwPoint.z : throwPoint.x) - 8.0) / 16.0;
        double uk = majorX ? uz / ux : ux / uz;
        double d = vk = majorX ? vz / vx : vx / vz;
        boolean rightPositive = majorPositive ? vk - uk > 0.0 : uk - vk > 0.0;
        int i = (int)(majorPositive ? Math.ceil(originMajor) : Math.floor(originMajor));
        do {
            double d2 = (double)i - originMajor;
            double d3 = majorX ? dx : dz;
            if (!(Math.abs(d2 / d3) < 312.5)) break;
            double minorU = originMinor + uk * ((double)i - originMajor);
            double minorV = originMinor + vk * ((double)i - originMajor);
            int j = (int)(rightPositive ? Math.ceil(minorU) : Math.floor(minorU));
            if (j < -1527) {
                j = -1527;
            }
            if (j > 1527) {
                j = 1527;
            }
            while ((rightPositive ? (double)j < minorV : (double)j > minorV) && j <= 1527 && j >= -1527) {
                int[] nArray;
                if (majorX) {
                    int[] nArray2 = new int[2];
                    nArray2[0] = i;
                    nArray = nArray2;
                    nArray2[1] = j;
                } else {
                    int[] nArray3 = new int[2];
                    nArray3[0] = j;
                    nArray = nArray3;
                    nArray3[1] = i;
                }
                chunks.add(nArray);
                j += rightPositive ? 1 : -1;
            }
            i += majorPositive ? 1 : -1;
        } while (chunks.size() <= 400000);
        return chunks;
    }

    private static void condition(double[] weights, List<int[]> candidates, EyeMeasurement measurement) {
        for (int i = 0; i < weights.length; ++i) {
            if (weights[i] <= 0.0) continue;
            int[] chunk = candidates.get(i);
            double dx = (double)(chunk[0] * 16 + 8) - measurement.x;
            double dz = (double)(chunk[1] * 16 + 8) - measurement.z;
            double gamma = -57.29577951308232 * Math.atan2(dx, dz);
            double delta = Math.abs((gamma - (double)measurement.yaw) % 360.0);
            delta = Math.min(delta, 360.0 - delta);
            double distanceSquared = dx * dx + dz * dz;
            double positionVariance = NinjabrainChunkCalculator.positionVariance(distanceSquared, measurement);
            double variance = measurement.sigma * measurement.sigma + positionVariance;
            int n = i;
            weights[n] = weights[n] * Math.exp(-(delta * delta) / (2.0 * variance));
        }
        NinjabrainChunkCalculator.normalise(weights);
    }

    private static double positionVariance(double distanceSquared, EyeMeasurement measurement) {
        if (NinjabrainChunkCalculator.isBlockCorner(measurement.x) && NinjabrainChunkCalculator.isBlockCorner(measurement.z)) {
            return 0.0;
        }
        return MAX_LATERAL_ERROR * MAX_LATERAL_ERROR / distanceSquared / 6.0;
    }

    private static boolean isBlockCorner(double coordinate) {
        double fraction = coordinate - Math.floor(coordinate);
        return Math.abs(fraction - 0.3) < 1.0E-6 || Math.abs(fraction - 0.7) < 1.0E-6;
    }

    private void applyClosestStrongholdCondition(double[] weights, List<int[]> candidates, EyeMeasurement throwPoint) {
        Integer[] order = new Integer[weights.length];
        for (int i = 0; i < order.length; ++i) {
            order[i] = i;
        }
        final double[] byWeight = weights;
        Arrays.sort(order, new Comparator<Integer>(){

            @Override
            public int compare(Integer a, Integer b) {
                return Double.compare(byWeight[b], byWeight[a]);
            }
        });
        double runningProbability = 0.0;
        int samples = 0;
        for (int rank = 0; rank < order.length; ++rank) {
            int index = order[rank];
            if (weights[index] <= 0.0) continue;
            if (rank < 100 || weights[index] > 0.001) {
                double probability = this.closestStrongholdProbability(candidates.get(index), throwPoint);
                int n = index;
                weights[n] = weights[n] * probability;
                runningProbability += probability;
                ++samples;
                continue;
            }
            if (samples <= 0) continue;
            int n = index;
            weights[n] = weights[n] * (runningProbability / (double)samples);
        }
        NinjabrainChunkCalculator.normalise(weights);
    }

    private double closestStrongholdProbability(int[] chunk, EyeMeasurement throwPoint) {
        double deltaX = (double)chunk[0] + (8.0 - throwPoint.x) / 16.0;
        double deltaZ = (double)chunk[1] + (8.0 - throwPoint.z) / 16.0;
        double playerRadius = Math.sqrt(throwPoint.x * throwPoint.x + throwPoint.z * throwPoint.z) / 16.0;
        double candidateDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        double phiCandidate = NinjabrainChunkCalculator.phi(chunk[0], chunk[1]);
        double phiPlayer = NinjabrainChunkCalculator.phi(throwPoint.x, throwPoint.z);
        double maxDistance = StrongholdRings.maxDistance(throwPoint.x, throwPoint.z) / 16.0;
        double minRadius = playerRadius - maxDistance;
        double maxRadius = playerRadius + maxDistance;
        int candidateRing = StrongholdRings.ringAt(Math.sqrt((double)chunk[0] * (double)chunk[0] + (double)chunk[1] * (double)chunk[1]));
        if (candidateRing < 0) {
            return 0.0;
        }
        double probability = 1.0;
        for (int ring = 0; ring < 8; ++ring) {
            if (maxRadius < StrongholdRings.innerRadius(ring) || minRadius > StrongholdRings.outerRadius(ring)) continue;
            boolean sameRing = candidateRing == ring;
            int strongholds = StrongholdRings.STRONGHOLDS_IN_RING[ring];
            double ak = StrongholdRings.innerRadius(candidateRing);
            double dphi = sameRing ? 2.0 * Math.sqrt(2.0) / ak : 0.41887902047863906 / (double)strongholds;
            for (int l = 0; l < strongholds; ++l) {
                if (sameRing && l == 0) continue;
                probability *= 1.0 - this.integral(ring, l, phiCandidate, dphi, phiPlayer, playerRadius, candidateDistance, sameRing);
            }
        }
        return probability;
    }

    private double integral(int ring, int l, double phiCandidate, double dphi, double phiPlayer, double playerRadius, double candidateDistance, boolean sameRing) {
        int strongholds = StrongholdRings.STRONGHOLDS_IN_RING[ring];
        double inner = StrongholdRings.innerRadius(ring);
        double innerPost = StrongholdRings.innerRadiusPostSnapping(ring);
        double outerPost = StrongholdRings.outerRadiusPostSnapping(ring);
        double phiMean = phiCandidate + (double)(l * 2) * Math.PI / (double)strongholds;
        double pdfIntegral = 0.0;
        double integral = 0.0;
        for (int k = -7; k <= 7; ++k) {
            double deltaPhi = (double)k * dphi;
            double pdf = 1.0;
            if (sameRing) {
                double scaled = deltaPhi * inner / (15.0 * Math.sqrt(2.0));
                pdf = Math.pow(1.0 + scaled, 4.5) * Math.pow(1.0 - scaled, 4.5);
            }
            pdfIntegral += pdf * dphi;
            double gamma = phiPlayer - (phiMean + (double)k * dphi);
            double sinBeta = playerRadius / candidateDistance * Math.sin(gamma);
            if (!(sinBeta < 1.0) || !(sinBeta > -1.0)) continue;
            double beta = Math.asin(sinBeta);
            double alpha0 = beta - gamma;
            double alpha1 = Math.PI - gamma - beta;
            double r0 = candidateDistance * Math.sin(alpha0) / Math.sin(gamma);
            double r1 = candidateDistance * Math.sin(alpha1) / Math.sin(gamma);
            if (r1 > outerPost) {
                r1 = outerPost;
            }
            if (r0 < innerPost) {
                r0 = innerPost;
            }
            if (r0 > outerPost) {
                r0 = outerPost;
            }
            if (r1 < innerPost) {
                r1 = innerPost;
            }
            integral += pdf * (StrongholdRings.cumulativePolar(r1) - StrongholdRings.cumulativePolar(r0)) * dphi / (double)strongholds;
        }
        if (pdfIntegral == 0.0) {
            return 0.0;
        }
        return (integral /= pdfIntegral) > 1.0 ? 1.0 : integral;
    }

    private static String advice(double[] weights, List<int[]> candidates, EyeMeasurement first) {
        double total = 0.0;
        for (int i = 0; i < weights.length; ++i) {
            total += weights[i];
        }
        if (!(total > 0.0)) {
            return null;
        }
        double[] distances = new double[weights.length];
        for (int i = 0; i < weights.length; ++i) {
            int[] chunk = candidates.get(i);
            double dx = (double)(chunk[0] * 16 + 8) - first.x;
            double dz = (double)(chunk[1] * 16 + 8) - first.z;
            distances[i] = Math.sqrt(dx * dx + dz * dz);
        }
        Integer[] order = new Integer[weights.length];
        for (int i = 0; i < order.length; ++i) {
            order[i] = i;
        }
        final double[] byDistance = distances;
        Arrays.sort(order, new Comparator<Integer>(){

            @Override
            public int compare(Integer a, Integer b) {
                return Double.compare(byDistance[a], byDistance[b]);
            }
        });
        double running = 0.0;
        double low = Double.NaN;
        double median = Double.NaN;
        double high = Double.NaN;
        for (int rank = 0; rank < order.length; ++rank) {
            int index = order[rank];
            running += weights[index];
            if (Double.isNaN(low) && running >= 0.05 * total) {
                low = distances[index];
            }
            if (Double.isNaN(median) && running >= 0.5 * total) {
                median = distances[index];
            }
            if (!(running >= 0.95 * total)) continue;
            high = distances[index];
            break;
        }
        if (Double.isNaN(median)) {
            return null;
        }
        if (Double.isNaN(high)) {
            high = distances[order[order.length - 1]];
        }
        double walk = NinjabrainChunkCalculator.travelAtAngle(median, first.sigma);
        double travelYaw = NinjabrainChunkCalculator.wrapYaw((double)first.yaw + 30.0);
        return "Stronghold is " + Math.round(low) + "-" + Math.round(high) + " blocks away\nFace yaw " + Math.round(travelYaw) + " and walk " + Math.round(walk) + ", then throw again";
    }

    private static double travelAtAngle(double distance, double sigmaDegrees) {
        double sigma = Math.toRadians(sigmaDegrees);
        double angle = Math.toRadians(30.0);
        double low = 1.0;
        double high = 20000.0;
        for (int i = 0; i < 200; ++i) {
            double mid = (low + high) / 2.0;
            double remaining = Math.max(distance - mid * Math.cos(angle), 1.0);
            double required = sigma * remaining * remaining / 4.0;
            if (required <= mid * Math.sin(angle)) {
                high = mid;
                continue;
            }
            low = mid;
        }
        return high < 50.0 ? 50.0 : high;
    }

    private static double wrapYaw(double yaw) {
        double wrapped = (yaw + 180.0) % 360.0;
        if (wrapped < 0.0) {
            wrapped += 360.0;
        }
        return wrapped - 180.0;
    }

    private static double phi(double x, double z) {
        return -Math.atan2(x, z);
    }

    private static void normalise(double[] weights) {
        int i;
        double total = 0.0;
        for (i = 0; i < weights.length; ++i) {
            total += weights[i];
        }
        if (total <= 0.0) {
            return;
        }
        i = 0;
        while (i < weights.length) {
            int n = i++;
            weights[n] = weights[n] / total;
        }
    }

    private static List<StrongholdPrediction> rank(double[] weights, List<int[]> candidates) {
        ArrayList<StrongholdPrediction> predictions = new ArrayList<StrongholdPrediction>();
        boolean[] taken = new boolean[weights.length];
        for (int pick = 0; pick < 3; ++pick) {
            int best = -1;
            double bestWeight = 0.0;
            for (int i = 0; i < weights.length; ++i) {
                if (taken[i] || !(weights[i] > bestWeight)) continue;
                bestWeight = weights[i];
                best = i;
            }
            if (best < 0 || pick > 0 && bestWeight < 0.01) break;
            int[] chunk = candidates.get(best);
            for (int i = 0; i < weights.length; ++i) {
                int[] other = candidates.get(i);
                if (Math.abs(other[0] - chunk[0]) > 1 || Math.abs(other[1] - chunk[1]) > 1) continue;
                taken[i] = true;
            }
            predictions.add(new StrongholdPrediction(chunk[0], chunk[1], bestWeight));
        }
        if (predictions.isEmpty()) {
            return Collections.emptyList();
        }
        return predictions;
    }
}

