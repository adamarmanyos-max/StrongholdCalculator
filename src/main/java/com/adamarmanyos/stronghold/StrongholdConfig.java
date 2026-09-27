package com.adamarmanyos.stronghold;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import net.fabricmc.loader.api.FabricLoader;

public final class StrongholdConfig {
    private static final String FILE_NAME = "strongholdfinder.properties";
    public static final int MIN_OPACITY = 10;
    public static final int MAX_OPACITY = 100;
    public static final int OPACITY_STEP = 10;
    public static final int[] SCALE_STEPS = new int[]{50, 75, 100, 125, 150, 200};
    public static final int MIN_SIGMA = 2;
    public static final int MAX_SIGMA = 50;
    private static final int DEFAULT_SIGMA = 6;
    private static int sigmaHundredths = 6;
    private static boolean trackEyeFlight;
    private static boolean boatEye;
    /**
     * Aim error assumed for a boat-eye throw, in ten-thousandths of a degree.
     * Ninjabrain Bot's default is 0.001 deg. It used to be thousandths with a
     * default of 0.01, which also applied to throws made without zooming in -
     * ten times too trusting for those, and not adjustable anywhere.
     */
    public static final int[] BOAT_SIGMA_STEPS = new int[]{5, 10, 15, 20, 30, 50};
    private static int boatEyeSigmaTenThousandths;
    private static int tallWidth;
    private static int tallHeight;
    private static boolean measuringTrainer;
    private static double calibrationSumOfSquares;
    private static int calibrationThrows;
    private static int opacityPercent;
    private static int scaleIndex;
    private static boolean loaded;

    private StrongholdConfig() {
    }

    public static int opacityPercent() {
        StrongholdConfig.ensureLoaded();
        return opacityPercent;
    }

    public static double angleSigma() {
        StrongholdConfig.ensureLoaded();
        return (double)sigmaHundredths / 100.0;
    }

    public static int sigmaHundredths() {
        StrongholdConfig.ensureLoaded();
        return sigmaHundredths;
    }

    public static void setSigmaHundredths(int value) {
        StrongholdConfig.ensureLoaded();
        int updated = value;
        if (updated < 2) {
            updated = 2;
        }
        if (updated > 50) {
            updated = 50;
        }
        if (updated != sigmaHundredths) {
            sigmaHundredths = updated;
            StrongholdConfig.save();
        }
    }

    public static int calibrationThrows() {
        StrongholdConfig.ensureLoaded();
        return calibrationThrows;
    }

    public static double addCalibrationErrors(double[] errorsInDegrees) {
        StrongholdConfig.ensureLoaded();
        if (errorsInDegrees == null || errorsInDegrees.length == 0) {
            return -1.0;
        }
        for (int i = 0; i < errorsInDegrees.length; ++i) {
            calibrationSumOfSquares += errorsInDegrees[i] * errorsInDegrees[i];
        }
        double estimate = Math.sqrt(calibrationSumOfSquares / ((double)(calibrationThrows += errorsInDegrees.length) - 0.5));
        sigmaHundredths = StrongholdConfig.clampSigma((int)Math.round(estimate * 100.0));
        StrongholdConfig.save();
        return (double)sigmaHundredths / 100.0;
    }

    public static void resetCalibration() {
        StrongholdConfig.ensureLoaded();
        calibrationSumOfSquares = 0.0;
        calibrationThrows = 0;
        StrongholdConfig.save();
    }

    public static boolean boatEye() {
        StrongholdConfig.ensureLoaded();
        return boatEye;
    }

    public static void setBoatEye(boolean value) {
        StrongholdConfig.ensureLoaded();
        if (value != boatEye) {
            boatEye = value;
            StrongholdConfig.save();
        }
    }

    public static double boatEyeSigma() {
        StrongholdConfig.ensureLoaded();
        return (double)boatEyeSigmaTenThousandths / 10000.0;
    }

    /** Steps to the next preset boat-eye aim error, wrapping round. */
    public static double cycleBoatEyeSigma() {
        StrongholdConfig.ensureLoaded();
        int next = BOAT_SIGMA_STEPS[0];
        for (int i = 0; i < BOAT_SIGMA_STEPS.length; ++i) {
            if (BOAT_SIGMA_STEPS[i] != boatEyeSigmaTenThousandths) continue;
            next = BOAT_SIGMA_STEPS[(i + 1) % BOAT_SIGMA_STEPS.length];
            break;
        }
        boatEyeSigmaTenThousandths = next;
        StrongholdConfig.save();
        return StrongholdConfig.boatEyeSigma();
    }

    public static int tallWidth() {
        StrongholdConfig.ensureLoaded();
        return tallWidth;
    }

    public static int tallHeight() {
        StrongholdConfig.ensureLoaded();
        return tallHeight;
    }

    public static boolean measuringTrainer() {
        StrongholdConfig.ensureLoaded();
        return measuringTrainer;
    }

    public static void setMeasuringTrainer(boolean value) {
        StrongholdConfig.ensureLoaded();
        if (value != measuringTrainer) {
            measuringTrainer = value;
            StrongholdConfig.save();
        }
    }

    public static boolean trackEyeFlight() {
        StrongholdConfig.ensureLoaded();
        return trackEyeFlight;
    }

    public static void setTrackEyeFlight(boolean value) {
        StrongholdConfig.ensureLoaded();
        if (value != trackEyeFlight) {
            trackEyeFlight = value;
            StrongholdConfig.save();
        }
    }

    public static int scalePercent() {
        StrongholdConfig.ensureLoaded();
        return SCALE_STEPS[scaleIndex];
    }

    public static float scaleFactor() {
        return (float)StrongholdConfig.scalePercent() / 100.0f;
    }

    public static int adjustScale(int direction) {
        StrongholdConfig.ensureLoaded();
        int updated = scaleIndex + direction;
        if (updated < 0) {
            updated = 0;
        }
        if (updated >= SCALE_STEPS.length) {
            updated = SCALE_STEPS.length - 1;
        }
        if (updated != scaleIndex) {
            scaleIndex = updated;
            StrongholdConfig.save();
        }
        return SCALE_STEPS[scaleIndex];
    }

    public static void setOpacityPercent(int percent) {
        StrongholdConfig.ensureLoaded();
        int updated = StrongholdConfig.clamp(percent);
        if (updated != opacityPercent) {
            opacityPercent = updated;
            StrongholdConfig.save();
        }
    }

    public static void setScalePercent(int percent) {
        StrongholdConfig.ensureLoaded();
        int updated = StrongholdConfig.nearestScaleIndex(percent);
        if (updated != scaleIndex) {
            scaleIndex = updated;
            StrongholdConfig.save();
        }
    }

    public static int scaleIndex() {
        StrongholdConfig.ensureLoaded();
        return scaleIndex;
    }

    public static void setScaleIndex(int index) {
        StrongholdConfig.ensureLoaded();
        int updated = index;
        if (updated < 0) {
            updated = 0;
        }
        if (updated >= SCALE_STEPS.length) {
            updated = SCALE_STEPS.length - 1;
        }
        if (updated != scaleIndex) {
            scaleIndex = updated;
            StrongholdConfig.save();
        }
    }

    public static int alphaBits() {
        int alpha = (int)Math.round((double)StrongholdConfig.opacityPercent() / 100.0 * 255.0);
        if (alpha < 26) {
            alpha = 26;
        }
        if (alpha > 255) {
            alpha = 255;
        }
        return alpha << 24;
    }

    public static int applyOpacity(int rgb) {
        return StrongholdConfig.alphaBits() | rgb & 0xFFFFFF;
    }

    public static int adjustOpacity(int deltaPercent) {
        StrongholdConfig.ensureLoaded();
        int updated = opacityPercent + deltaPercent;
        if (updated < 10) {
            updated = 10;
        }
        if (updated > 100) {
            updated = 100;
        }
        if (updated != opacityPercent) {
            opacityPercent = updated;
            StrongholdConfig.save();
        }
        return opacityPercent;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        try {
            Path path = StrongholdConfig.configPath();
            if (!Files.exists(path, new LinkOption[0])) {
                return;
            }
            try (BufferedReader reader = Files.newBufferedReader(path);){
                String line;
                while ((line = reader.readLine()) != null) {
                    int equals = line.indexOf(61);
                    if (equals <= 0) continue;
                    String key = line.substring(0, equals).trim();
                    String value = line.substring(equals + 1).trim();
                    if ("opacity".equals(key)) {
                        opacityPercent = StrongholdConfig.clamp(Integer.parseInt(value));
                        continue;
                    }
                    if ("scale".equals(key)) {
                        scaleIndex = StrongholdConfig.nearestScaleIndex(Integer.parseInt(value));
                        continue;
                    }
                    if ("sigma".equals(key)) {
                        sigmaHundredths = StrongholdConfig.clampSigma(Integer.parseInt(value));
                        continue;
                    }
                    if ("trackeye".equals(key)) {
                        trackEyeFlight = "true".equalsIgnoreCase(value);
                        continue;
                    }
                    if ("boat".equals(key)) {
                        boatEye = "true".equalsIgnoreCase(value);
                        continue;
                    }
                    if ("tallwidth".equals(key)) {
                        tallWidth = Math.max(160, Integer.parseInt(value));
                        continue;
                    }
                    if ("tallheight".equals(key)) {
                        tallHeight = Math.max(240, Integer.parseInt(value));
                        continue;
                    }
                    if ("boatsigma_e4".equals(key)) {
                        boatEyeSigmaTenThousandths = Math.max(1, Math.min(1000, Integer.parseInt(value)));
                        continue;
                    }
                    if ("trainer".equals(key)) {
                        measuringTrainer = "true".equalsIgnoreCase(value);
                        continue;
                    }
                    if ("calib_sumsq".equals(key)) {
                        calibrationSumOfSquares = Double.parseDouble(value);
                        continue;
                    }
                    if (!"calib_n".equals(key)) continue;
                    calibrationThrows = Integer.parseInt(value);
                }
            }
        }
        catch (IOException path) {
        }
        catch (NumberFormatException failed) {
            opacityPercent = 100;
            scaleIndex = 2;
            sigmaHundredths = 6;
            trackEyeFlight = false;
            calibrationSumOfSquares = 0.0;
            calibrationThrows = 0;
            boatEye = true;
            boatEyeSigmaTenThousandths = 10;
            tallWidth = 512;
            tallHeight = 4096;
            measuringTrainer = false;
        }
    }

    private static void save() {
        try {
            Path path = StrongholdConfig.configPath();
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent, new FileAttribute[0]);
            }
            try (BufferedWriter writer = Files.newBufferedWriter(path, new OpenOption[0]);){
                writer.write("opacity=" + opacityPercent);
                writer.newLine();
                writer.write("scale=" + SCALE_STEPS[scaleIndex]);
                writer.newLine();
                writer.write("sigma=" + sigmaHundredths);
                writer.newLine();
                writer.write("trackeye=" + trackEyeFlight);
                writer.newLine();
                writer.write("boat=" + boatEye);
                writer.newLine();
                writer.write("boatsigma_e4=" + boatEyeSigmaTenThousandths);
                writer.newLine();
                writer.write("tallwidth=" + tallWidth);
                writer.newLine();
                writer.write("tallheight=" + tallHeight);
                writer.newLine();
                writer.write("trainer=" + measuringTrainer);
                writer.newLine();
                writer.write("calib_sumsq=" + calibrationSumOfSquares);
                writer.newLine();
                writer.write("calib_n=" + calibrationThrows);
                writer.newLine();
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    private static int nearestScaleIndex(int percent) {
        int best = 0;
        int bestDistance = Integer.MAX_VALUE;
        for (int i = 0; i < SCALE_STEPS.length; ++i) {
            int distance = Math.abs(SCALE_STEPS[i] - percent);
            if (distance >= bestDistance) continue;
            bestDistance = distance;
            best = i;
        }
        return best;
    }

    private static int clampSigma(int value) {
        if (value < 2) {
            return 2;
        }
        if (value > 50) {
            return 50;
        }
        return value;
    }

    private static int clamp(int value) {
        if (value < 10) {
            return 10;
        }
        if (value > 100) {
            return 100;
        }
        return value;
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
    }

    static {
        boatEye = true;
        boatEyeSigmaTenThousandths = 10;
        tallWidth = 512;
        tallHeight = 4096;
        opacityPercent = 100;
        scaleIndex = 2;
    }
}

