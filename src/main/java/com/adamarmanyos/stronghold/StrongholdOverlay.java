package com.adamarmanyos.stronghold;

import com.adamarmanyos.stronghold.BlindPractice;
import com.adamarmanyos.stronghold.EyeTracker;
import com.adamarmanyos.stronghold.PixelPerfect;
import com.adamarmanyos.stronghold.StrongholdConfig;
import com.adamarmanyos.stronghold.StrongholdSettingsScreen;
import com.adamarmanyos.stronghold.TallScreen;
import com.adamarmanyos.stronghold.calc.EyeMeasurement;
import com.adamarmanyos.stronghold.calc.NinjabrainChunkCalculator;
import com.adamarmanyos.stronghold.calc.StrongholdCalculator;
import com.adamarmanyos.stronghold.calc.StrongholdPrediction;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.network.ClientPlayerEntity;

public final class StrongholdOverlay {
    private static final int KEY_MODIFIER = 292;
    private static final int KEY_CAPTURE = 67;
    private static final int KEY_TOGGLE = 78;
    private static final int KEY_SETTINGS = 46;
    private static final int KEY_MEASURE_MODE = 77;
    private static final int KEY_CALIBRATE = 75;
    private static final int KEY_TELEPORT = 80;
    private static final int KEY_TALL_SCREEN = 265;
    private static final int KEY_PIXEL_MINUS = 263;
    private static final int KEY_PIXEL_PLUS = 262;
    private static final double MEASURE_FOV = 30.0;
    private static final double MEASURE_SENSITIVITY = 0.0;
    private static final double TRAVEL_FOV = 110.0;
    private static final double TRAVEL_SENSITIVITY = 0.4;
    private static final double MEASURE_SENSITIVITY_THRESHOLD = 0.25;
    private static final int KEY_RESET = 66;
    private static final int KEY_FADE = 91;
    private static final int KEY_BRIGHTEN = 93;
    private static final int KEY_SMALLER = 45;
    private static final int KEY_LARGER = 61;
    private static final int PANEL_X = 4;
    private static final int PANEL_Y = 4;
    private static final int PANEL_WIDTH = 256;
    private static final int PADDING = 5;
    private static final int TITLE_BAR_HEIGHT = 14;
    private static final int ROW_HEIGHT = 11;
    private static final int COL_COORD_CENTRE = 43;
    private static final int COL_PERCENT_RIGHT = 119;
    private static final int COL_DIST_RIGHT = 155;
    private static final int COL_NETHER_CENTRE = 199;
    private static final int COL_THROW_X_RIGHT = 84;
    private static final int COL_THROW_Z_RIGHT = 146;
    private static final int COL_THROW_ANGLE_RIGHT = 198;
    private static final int COL_THROW_ERROR_RIGHT = 251;
    private static final int PANEL_BACKGROUND = 1842980;
    private static final int TITLE_BAR_BACKGROUND = 2896184;
    private static final int SEPARATOR = 3817287;
    private static final int COLOR_TITLE = 15790837;
    private static final int COLOR_VERSION = 8028812;
    private static final int COLOR_HEADER = 11845324;
    private static final int COLOR_COORD = 9414856;
    private static final int COLOR_NUMBER = 14212322;
    private static final int COLOR_SECTION = 15790837;
    private static final int COLOR_HIGH = 6014059;
    private static final int COLOR_MEDIUM = 14723132;
    private static final int COLOR_LOW = 12733002;
    private static final int COLOR_NOTICE = 16765514;
    private static final int COLOR_WARNING = 16743002;
    private static final double MIN_USEFUL_BASELINE = 100.0;
    private static final double FIT_WARNING_RATIO = 2.0;
    private static final double PERCENT_HIGH = 50.0;
    private static final double PERCENT_MEDIUM = 5.0;
    private static final double ERROR_GOOD = 0.01;
    private static final double ERROR_FAIR = 0.05;
    private static final long NOTICE_DURATION_MS = 3000L;
    private static final int MAX_MEASUREMENTS = 3;
    private static final String VERSION = "v6.1.0";
    private static final StrongholdCalculator CALCULATOR = new NinjabrainChunkCalculator();
    private static final List<EyeMeasurement> MEASUREMENTS = new ArrayList<EyeMeasurement>();
    private static List<StrongholdPrediction> results;
    private static String advice;
    private static String boatScore;
    private static boolean hidden;
    private static String notice;
    private static long noticeExpiresAt;
    private static boolean prevCapture;
    private static boolean prevToggle;
    private static boolean prevSettings;
    private static boolean prevMeasureMode;
    private static boolean prevCalibrate;
    private static boolean prevTeleport;
    private static boolean prevTallScreen;
    private static boolean prevPixelMinus;
    private static boolean prevPixelPlus;
    private static boolean prevReset;
    private static boolean prevFade;
    private static boolean prevBrighten;
    private static boolean prevSmaller;
    private static boolean prevLarger;

    private StrongholdOverlay() {
    }

    public static void render(MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }
        EyeTracker.tick(client);
        BlindPractice.tick(client);
        StrongholdOverlay.pollKeys(client);
        float scale = StrongholdConfig.scaleFactor();
        if (scale == 1.0f) {
            StrongholdOverlay.draw(client, matrices);
            return;
        }
        matrices.push();
        matrices.scale(scale, scale, 1.0f);
        try {
            StrongholdOverlay.draw(client, matrices);
        }
        finally {
            matrices.pop();
        }
    }

    private static void pollKeys(MinecraftClient client) {
        boolean larger;
        if (client.currentScreen != null) {
            prevCapture = false;
            prevToggle = false;
            prevSettings = false;
            prevMeasureMode = false;
            prevCalibrate = false;
            prevTeleport = false;
            prevTallScreen = false;
            prevPixelMinus = false;
            prevPixelPlus = false;
            prevReset = false;
            prevFade = false;
            prevBrighten = false;
            prevSmaller = false;
            prevLarger = false;
            return;
        }
        long window = client.getWindow().getHandle();
        boolean modifier = InputUtil.isKeyPressed((long)window, (int)292);
        boolean capture = modifier && InputUtil.isKeyPressed((long)window, (int)67);
        boolean toggle = !modifier && InputUtil.isKeyPressed((long)window, (int)78);
        boolean settings = !modifier && InputUtil.isKeyPressed((long)window, (int)46);
        boolean measureMode = !modifier && InputUtil.isKeyPressed((long)window, (int)77);
        boolean calibrate = !modifier && InputUtil.isKeyPressed((long)window, (int)75);
        boolean teleport = !modifier && InputUtil.isKeyPressed((long)window, (int)80);
        boolean tallScreen = !modifier && InputUtil.isKeyPressed((long)window, (int)265);
        boolean pixelMinus = !modifier && InputUtil.isKeyPressed((long)window, (int)263);
        boolean pixelPlus = !modifier && InputUtil.isKeyPressed((long)window, (int)262);
        boolean reset = !modifier && InputUtil.isKeyPressed((long)window, (int)66);
        boolean fade = !modifier && InputUtil.isKeyPressed((long)window, (int)91);
        boolean brighten = !modifier && InputUtil.isKeyPressed((long)window, (int)93);
        boolean smaller = !modifier && InputUtil.isKeyPressed((long)window, (int)45);
        boolean bl = larger = !modifier && InputUtil.isKeyPressed((long)window, (int)61);
        if (capture && !prevCapture) {
            StrongholdOverlay.capture(client.player);
        }
        if (toggle && !prevToggle) {
            hidden = !hidden;
            StrongholdOverlay.showNotice(hidden ? "Panel hidden" : "Panel shown");
        }
        if (settings && !prevSettings) {
            client.openScreen(new StrongholdSettingsScreen());
        }
        if (measureMode && !prevMeasureMode) {
            StrongholdOverlay.toggleMeasureMode(client);
        }
        if (calibrate && !prevCalibrate) {
            StrongholdOverlay.calibrateHere(client.player);
        }
        if (teleport && !prevTeleport) {
            StrongholdOverlay.teleportToPrediction(client.player);
        }
        if (tallScreen && !prevTallScreen) {
            TallScreen.toggle(client);
        }
        if (pixelMinus && !prevPixelMinus) {
            StrongholdOverlay.adjustLastAngle(client, -1);
        }
        if (pixelPlus && !prevPixelPlus) {
            StrongholdOverlay.adjustLastAngle(client, 1);
        }
        if (reset && !prevReset) {
            StrongholdOverlay.reset();
        }
        if (fade && !prevFade) {
            StrongholdOverlay.showNotice("Opacity " + StrongholdConfig.adjustOpacity(-10) + "%");
        }
        if (brighten && !prevBrighten) {
            StrongholdOverlay.showNotice("Opacity " + StrongholdConfig.adjustOpacity(10) + "%");
        }
        if (smaller && !prevSmaller) {
            StrongholdOverlay.showNotice("Size " + StrongholdConfig.adjustScale(-1) + "%");
        }
        if (larger && !prevLarger) {
            StrongholdOverlay.showNotice("Size " + StrongholdConfig.adjustScale(1) + "%");
        }
        prevCapture = capture;
        prevToggle = toggle;
        prevSettings = settings;
        prevMeasureMode = measureMode;
        prevCalibrate = calibrate;
        prevTeleport = teleport;
        prevTallScreen = tallScreen;
        prevPixelMinus = pixelMinus;
        prevPixelPlus = pixelPlus;
        prevReset = reset;
        prevFade = fade;
        prevBrighten = brighten;
        prevSmaller = smaller;
        prevLarger = larger;
    }

    private static void toggleMeasureMode(MinecraftClient client) {
        boolean measuring;
        GameOptions options = client.options;
        if (options == null) {
            return;
        }
        boolean bl = measuring = options.mouseSensitivity <= 0.25;
        if (measuring) {
            options.fov = 110.0;
            options.mouseSensitivity = 0.4;
            StrongholdOverlay.showNotice("Travel: FOV 110, sens 80%");
        } else {
            options.fov = 30.0;
            options.mouseSensitivity = 0.0;
            StrongholdOverlay.showNotice("Measure: FOV 30, slow mouse");
        }
        options.write();
    }

    private static void adjustLastAngle(MinecraftClient client, int increments) {
        if (MEASUREMENTS.isEmpty()) {
            StrongholdOverlay.showNotice("No throw to adjust");
            return;
        }
        EyeMeasurement last = MEASUREMENTS.get(MEASUREMENTS.size() - 1);
        double perPixel = PixelPerfect.degreesPerPixel(client, last.pitch);
        last.yaw = (float)((double)last.yaw + (double)increments * perPixel);
        last.pixelAdjustments += increments;
        StrongholdOverlay.showNotice("Angle " + (last.pixelAdjustments >= 0 ? "+" : "") + last.pixelAdjustments + " px (" + StrongholdOverlay.decimals(perPixel * (double)last.pixelAdjustments, 4) + " deg)");
        StrongholdOverlay.calculate();
    }

    private static void teleportToPrediction(ClientPlayerEntity player) {
        if (results == null || results.isEmpty()) {
            StrongholdOverlay.showNotice("Nothing to teleport to");
            return;
        }
        if (!player.isCreative()) {
            StrongholdOverlay.showNotice("Teleport needs creative mode");
            return;
        }
        StrongholdPrediction best = results.get(0);
        String command = "/tp @s " + best.overworldX + " " + Math.round(player.getY()) + " " + best.overworldZ;
        player.sendChatMessage(command);
        StrongholdOverlay.showNotice("Teleporting to " + best.overworldX + ", " + best.overworldZ);
    }

    private static void calibrateHere(ClientPlayerEntity player) {
        if (MEASUREMENTS.isEmpty()) {
            StrongholdOverlay.showNotice("No throws to calibrate from");
            return;
        }
        int chunkX = Math.floorDiv((int)Math.floor(player.getX()), 16);
        int chunkZ = Math.floorDiv((int)Math.floor(player.getZ()), 16);
        double targetX = chunkX * 16 + 8;
        double targetZ = chunkZ * 16 + 8;
        double[] errors = new double[MEASUREMENTS.size()];
        for (int i = 0; i < MEASUREMENTS.size(); ++i) {
            EyeMeasurement measurement = MEASUREMENTS.get(i);
            double predicted = Math.toDegrees(Math.atan2(-(targetX - measurement.x), targetZ - measurement.z));
            errors[i] = StrongholdOverlay.wrapYaw(predicted - (double)measurement.yaw);
        }
        double sigma = StrongholdConfig.addCalibrationErrors(errors);
        if (sigma < 0.0) {
            StrongholdOverlay.showNotice("Calibration failed");
            return;
        }
        StrongholdOverlay.showNotice("Calibrated: sigma " + StrongholdOverlay.decimals(sigma, 2) + " from " + StrongholdConfig.calibrationThrows() + " throws");
    }

    private static void capture(ClientPlayerEntity player) {
        String detail;
        EyeMeasurement measurement;
        boolean useTracking = StrongholdConfig.trackEyeFlight();
        if (useTracking && EyeTracker.hasBearing() && EyeTracker.isCaptured()) {
            StrongholdOverlay.showNotice("That eye is already added");
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (useTracking && EyeTracker.hasBearing()) {
            measurement = new EyeMeasurement(EyeTracker.originX(), EyeTracker.originZ(), (float)EyeTracker.bearingDegrees(), 0.005, true);
        } else if (StrongholdConfig.boatEye()) {
            measurement = new EyeMeasurement(player.getX(), player.getZ(), (float)((double)player.yaw - PixelPerfect.packetRoundingCorrection(player.yaw)), StrongholdConfig.boatEyeSigma(), false);
            measurement.fromBoat = true;
        } else {
            measurement = new EyeMeasurement(player.getX(), player.getZ(), (float)((double)player.yaw - PixelPerfect.packetRoundingCorrection(player.yaw)), StrongholdConfig.angleSigma(), false);
        }
        measurement.pitch = player.pitch;
        String string = detail = measurement.fromEyeFlight ? " tracked over " + StrongholdOverlay.decimals(EyeTracker.travelled(), 0) + " blocks" : " from crosshair - less accurate";
        if (StrongholdConfig.measuringTrainer() && EyeTracker.hasBearing()) {
            double error = StrongholdOverlay.wrapYaw((double)measurement.yaw - EyeTracker.bearingDegrees());
            boatScore = "Last throw off by " + StrongholdOverlay.decimals(Math.abs(error), 4) + " deg";
        }
        EyeTracker.markCaptured();
        StrongholdOverlay.record(measurement, detail);
    }

    private static void record(EyeMeasurement measurement, String detail) {
        MEASUREMENTS.add(measurement);
        hidden = false;
        StrongholdOverlay.showNotice("Eye " + MEASUREMENTS.size() + detail);
        while (MEASUREMENTS.size() > 3) {
            MEASUREMENTS.remove(0);
        }
        StrongholdOverlay.calculate();
    }

    private static void calculate() {
        if (MEASUREMENTS.size() < CALCULATOR.minimumMeasurements()) {
            StrongholdOverlay.showNotice("Need an Eye measurement");
            return;
        }
        StrongholdCalculator.Result calculated = CALCULATOR.calculate(MEASUREMENTS);
        if (!calculated.isSuccess()) {
            results = null;
            advice = null;
            StrongholdOverlay.showNotice(calculated.error());
            return;
        }
        results = calculated.candidates();
        advice = calculated.advice;
        notice = null;
    }

    private static void reset() {
        MEASUREMENTS.clear();
        results = null;
        advice = null;
        notice = null;
        hidden = false;
        EyeTracker.forget();
    }

    public static void notice(String text) {
        StrongholdOverlay.showNotice(text);
    }

    private static void showNotice(String text) {
        notice = text;
        noticeExpiresAt = System.currentTimeMillis() + 3000L;
    }

    private static void draw(MinecraftClient client, MatrixStack matrices) {
        boolean hasContent;
        boolean bl = hasContent = !hidden && (!MEASUREMENTS.isEmpty() || StrongholdConfig.trackEyeFlight() && EyeTracker.isReady() || results != null && !results.isEmpty());
        if (hasContent) {
            StrongholdOverlay.drawPanel(client, matrices);
        }
        if (notice != null) {
            if (System.currentTimeMillis() > noticeExpiresAt) {
                notice = null;
            } else {
                int y = hasContent ? 4 + StrongholdOverlay.panelHeight() + 4 : 4;
                StrongholdOverlay.text(client, matrices, notice, 9, y, 16765514);
            }
        }
    }

    private static int adviceLineCount() {
        if (advice == null) {
            return 0;
        }
        int lines = 1;
        for (int i = 0; i < advice.length(); ++i) {
            if (advice.charAt(i) != '\n') continue;
            ++lines;
        }
        return lines;
    }

    private static int candidateRows() {
        if (results == null || MEASUREMENTS.size() < 2) {
            return 0;
        }
        return results.size();
    }

    private static int panelHeight() {
        int height = 14;
        if (StrongholdOverlay.candidateRows() > 0) {
            height += 3;
            height += 11;
            height += StrongholdOverlay.candidateRows() * 11;
            height += 4;
        }
        ++height;
        height += 3;
        height += 11;
        height += 11;
        height += MEASUREMENTS.size() * 11;
        if (advice != null && MEASUREMENTS.size() < 2) {
            height += 11 * StrongholdOverlay.adviceLineCount() + 2;
        }
        if (StrongholdConfig.measuringTrainer()) {
            int lines = 3;
            if (!MEASUREMENTS.isEmpty()) {
                ++lines;
            }
            if (boatScore != null) {
                ++lines;
            }
            height += 11 * lines + 2;
        }
        if (StrongholdConfig.trackEyeFlight() && EyeTracker.isReady()) {
            height += 13;
        }
        if (StrongholdOverlay.warning() != null) {
            height += 13;
        }
        return height += 5;
    }

    private static void drawPanel(MinecraftClient client, MatrixStack matrices) {
        String warning;
        int left = 4;
        int top = 4;
        int right = left + 256;
        int bottom = top + StrongholdOverlay.panelHeight();
        StrongholdOverlay.roundedRect(matrices, left, top, right, bottom, 1842980);
        StrongholdOverlay.roundedTop(matrices, left, top, right, top + 14, 2896184);
        int y = top + 3;
        StrongholdOverlay.text(client, matrices, "Stronghold Finder", left + 5, y, 15790837);
        int titleWidth = client.textRenderer.getWidth("Stronghold Finder");
        StrongholdOverlay.text(client, matrices, VERSION, left + 5 + titleWidth + 4, y, 8028812);
        y = top + 14;
        if (StrongholdOverlay.candidateRows() > 0) {
            StrongholdOverlay.centred(client, matrices, "Overworld", left + 43, y += 3, 11845324);
            StrongholdOverlay.rightAligned(client, matrices, "%", left + 119, y, 11845324);
            StrongholdOverlay.rightAligned(client, matrices, "Dist.", left + 155, y, 11845324);
            StrongholdOverlay.centred(client, matrices, "Nether", left + 199, y, 11845324);
            y += 11;
            for (int i = 0; i < results.size(); ++i) {
                StrongholdPrediction candidate = results.get(i);
                String overworld = "(" + candidate.overworldX + ", " + candidate.overworldZ + ")";
                String nether = "(" + candidate.netherX() + ", " + candidate.netherZ() + ")";
                double dx = (double)candidate.overworldX - client.player.getX();
                double dz = (double)candidate.overworldZ - client.player.getZ();
                long distance = Math.round(Math.sqrt(dx * dx + dz * dz));
                StrongholdOverlay.centred(client, matrices, overworld, left + 43, y, 9414856);
                if (candidate.hasCertainty()) {
                    double percent = candidate.certainty * 100.0;
                    StrongholdOverlay.rightAligned(client, matrices, StrongholdOverlay.decimals(percent, 1) + "%", left + 119, y, StrongholdOverlay.percentColor(percent));
                } else {
                    StrongholdOverlay.rightAligned(client, matrices, "-", left + 119, y, 14212322);
                }
                StrongholdOverlay.rightAligned(client, matrices, Long.toString(distance), left + 155, y, 14212322);
                StrongholdOverlay.centred(client, matrices, nether, left + 199, y, 9414856);
                y += 11;
            }
            y += 4;
        }
        StrongholdOverlay.fill(matrices, left + 5, y, right - 5, y + 1, 3817287);
        StrongholdOverlay.text(client, matrices, "Ender eye throws", left + 5, y += 4, 15790837);
        StrongholdOverlay.text(client, matrices, "Src", left + 5, y += 11, 11845324);
        StrongholdOverlay.rightAligned(client, matrices, "x", left + 84, y, 11845324);
        StrongholdOverlay.rightAligned(client, matrices, "z", left + 146, y, 11845324);
        StrongholdOverlay.rightAligned(client, matrices, "Angle", left + 198, y, 11845324);
        StrongholdOverlay.rightAligned(client, matrices, "Error", left + 251, y, 11845324);
        y += 11;
        StrongholdPrediction best = results == null || results.isEmpty() ? null : results.get(0);
        for (int i = 0; i < MEASUREMENTS.size(); ++i) {
            String source;
            EyeMeasurement measurement = MEASUREMENTS.get(i);
            String string = measurement.fromEyeFlight ? "eye" : (source = measurement.fromBoat ? "boat" : "aim");
            int sourceColor = measurement.fromEyeFlight ? 6014059 : (measurement.fromBoat ? 6014059 : 12733002);
            StrongholdOverlay.text(client, matrices, source, left + 5, y, sourceColor);
            StrongholdOverlay.rightAligned(client, matrices, StrongholdOverlay.decimals(measurement.x, 2), left + 84, y, 9414856);
            StrongholdOverlay.rightAligned(client, matrices, StrongholdOverlay.decimals(measurement.z, 2), left + 146, y, 9414856);
            StrongholdOverlay.rightAligned(client, matrices, StrongholdOverlay.decimals(StrongholdOverlay.wrapYaw(measurement.yaw), 2), left + 198, y, measurement.fromEyeFlight ? 6014059 : 14212322);
            if (best == null || MEASUREMENTS.size() < 2) {
                StrongholdOverlay.rightAligned(client, matrices, "-", left + 251, y, 14212322);
            } else {
                double error = StrongholdOverlay.angleError(measurement, best);
                StrongholdOverlay.rightAligned(client, matrices, StrongholdOverlay.decimals(error, 3), left + 251, y, StrongholdOverlay.errorColor(error));
            }
            y += 11;
        }
        if (advice != null && MEASUREMENTS.size() < 2) {
            y += 2;
            for (String line : advice.split("\n")) {
                StrongholdOverlay.text(client, matrices, line, left + 5, y, 11845324);
                y += 11;
            }
        }
        if (StrongholdConfig.measuringTrainer()) {
            boolean fovOk = PixelPerfect.atMeasuringFov(client);
            double perPixel = PixelPerfect.degreesPerPixel(client, client.player.pitch);
            boolean corner = PixelPerfect.isOnCorner(client.player.getX(), client.player.getZ());
            StrongholdOverlay.text(client, matrices, "FOV " + Math.round(client.options.fov) + (fovOk ? " ok" : " - press M, pixel maths needs 30") + "   " + StrongholdOverlay.decimals(perPixel, 4) + " deg/px", left + 5, y += 2, fovOk ? 11845324 : 12733002);
            int screenHeight = client.getWindow().getHeight();
            boolean tallEnough = screenHeight >= 3071;
            StrongholdOverlay.text(client, matrices, "Screen " + client.getWindow().getWidth() + "x" + screenHeight + (tallEnough ? "  pixel beats 0.01" : "  too short (up arrow)"), left + 5, y += 11, tallEnough ? 6014059 : 14723132);
            StrongholdOverlay.text(client, matrices, corner ? "On a block corner - position is exact" : "Not on a corner - stand against a block", left + 5, y += 11, corner ? 6014059 : 14723132);
            y += 11;
            if (!MEASUREMENTS.isEmpty()) {
                EyeMeasurement last = MEASUREMENTS.get(MEASUREMENTS.size() - 1);
                StrongholdOverlay.text(client, matrices, "Last throw " + (last.pixelAdjustments >= 0 ? "+" : "") + last.pixelAdjustments + " px  (arrows adjust)", left + 5, y, 14212322);
                y += 11;
            }
            if (boatScore != null) {
                StrongholdOverlay.text(client, matrices, boatScore, left + 5, y, 14212322);
                y += 11;
            }
        }
        if ((warning = StrongholdOverlay.warning()) != null) {
            StrongholdOverlay.text(client, matrices, warning, left + 5, y += 2, 16743002);
        }
    }

    private static String warning() {
        if (MEASUREMENTS.size() < 2 || results == null || results.isEmpty()) {
            return null;
        }
        double baseline = StrongholdOverlay.longestBaseline();
        if (baseline < 100.0) {
            return "Throws " + Math.round(baseline) + " blocks apart - move further";
        }
        StrongholdPrediction best = results.get(0);
        double worst = 0.0;
        double chiSquare = 0.0;
        for (int i = 0; i < MEASUREMENTS.size(); ++i) {
            double error = StrongholdOverlay.angleError(MEASUREMENTS.get(i), best);
            double scaled = error / StrongholdOverlay.MEASUREMENTS.get((int)i).sigma;
            chiSquare += scaled * scaled;
            if (!(Math.abs(error) > worst)) continue;
            worst = Math.abs(error);
        }
        if (chiSquare > 2.0 * (double)MEASUREMENTS.size()) {
            return "Angles off by " + StrongholdOverlay.decimals(worst, 2) + " deg - retake";
        }
        return null;
    }

    private static double longestBaseline() {
        double longest = 0.0;
        for (int i = 0; i < MEASUREMENTS.size(); ++i) {
            for (int j = i + 1; j < MEASUREMENTS.size(); ++j) {
                EyeMeasurement a = MEASUREMENTS.get(i);
                EyeMeasurement b = MEASUREMENTS.get(j);
                double distance = Math.hypot(a.x - b.x, a.z - b.z);
                if (!(distance > longest)) continue;
                longest = distance;
            }
        }
        return longest;
    }

    private static double angleError(EyeMeasurement measurement, StrongholdPrediction prediction) {
        double predicted = Math.toDegrees(Math.atan2(-((double)prediction.overworldX - measurement.x), (double)prediction.overworldZ - measurement.z));
        double difference = (predicted - (double)measurement.yaw + 180.0) % 360.0;
        if (difference < 0.0) {
            difference += 360.0;
        }
        return difference - 180.0;
    }

    private static int percentColor(double percent) {
        if (percent >= 50.0) {
            return 6014059;
        }
        if (percent >= 5.0) {
            return 14723132;
        }
        return 12733002;
    }

    private static int errorColor(double error) {
        double magnitude = Math.abs(error);
        if (magnitude <= 0.01) {
            return 6014059;
        }
        if (magnitude <= 0.05) {
            return 14723132;
        }
        return 12733002;
    }

    private static void fill(MatrixStack matrices, int x1, int y1, int x2, int y2, int rgb) {
        DrawableHelper.fill(matrices, (int)x1, (int)y1, (int)x2, (int)y2, (int)StrongholdConfig.applyOpacity(rgb));
    }

    private static void roundedRect(MatrixStack matrices, int left, int top, int right, int bottom, int rgb) {
        StrongholdOverlay.fill(matrices, left + 3, top, right - 3, top + 1, rgb);
        StrongholdOverlay.fill(matrices, left + 1, top + 1, right - 1, top + 3, rgb);
        StrongholdOverlay.fill(matrices, left, top + 3, right, bottom - 3, rgb);
        StrongholdOverlay.fill(matrices, left + 1, bottom - 3, right - 1, bottom - 1, rgb);
        StrongholdOverlay.fill(matrices, left + 3, bottom - 1, right - 3, bottom, rgb);
    }

    private static void roundedTop(MatrixStack matrices, int left, int top, int right, int bottom, int rgb) {
        StrongholdOverlay.fill(matrices, left + 3, top, right - 3, top + 1, rgb);
        StrongholdOverlay.fill(matrices, left + 1, top + 1, right - 1, top + 3, rgb);
        StrongholdOverlay.fill(matrices, left, top + 3, right, bottom, rgb);
    }

    private static void text(MinecraftClient client, MatrixStack matrices, String value, int x, int y, int rgb) {
        client.textRenderer.draw(matrices, value, (float)x, (float)y, StrongholdConfig.applyOpacity(rgb));
    }

    private static void centred(MinecraftClient client, MatrixStack matrices, String value, int centreX, int y, int rgb) {
        int width = client.textRenderer.getWidth(value);
        StrongholdOverlay.text(client, matrices, value, centreX - width / 2, y, rgb);
    }

    private static void rightAligned(MinecraftClient client, MatrixStack matrices, String value, int rightX, int y, int rgb) {
        int width = client.textRenderer.getWidth(value);
        StrongholdOverlay.text(client, matrices, value, rightX - width, y, rgb);
    }

    private static double wrapYaw(double yaw) {
        double wrapped = (yaw + 180.0) % 360.0;
        if (wrapped < 0.0) {
            wrapped += 360.0;
        }
        return wrapped - 180.0;
    }

    private static String decimals(double value, int places) {
        boolean negative;
        long scale = 1L;
        for (int i = 0; i < places; ++i) {
            scale *= 10L;
        }
        long scaled = Math.round(value * (double)scale);
        boolean bl = negative = scaled < 0L;
        if (negative) {
            scaled = -scaled;
        }
        Object fraction = Long.toString(scaled % scale);
        while (((String)fraction).length() < places) {
            fraction = "0" + (String)fraction;
        }
        String text = scaled / scale + "." + (String)fraction;
        return negative ? "-" + text : text;
    }

    public static String calculatorName() {
        return CALCULATOR.name();
    }
}

