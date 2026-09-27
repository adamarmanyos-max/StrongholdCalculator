package com.adamarmanyos.stronghold;

import com.adamarmanyos.stronghold.calc.EyeMeasurement;
import com.adamarmanyos.stronghold.calc.NinjabrainChunkCalculator;
import com.adamarmanyos.stronghold.calc.StrongholdCalculator;
import com.adamarmanyos.stronghold.calc.StrongholdPrediction;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;

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
    private static final int KEY_RESET = 66;
    private static final int KEY_FADE = 91;
    private static final int KEY_BRIGHTEN = 93;
    private static final int KEY_SMALLER = 45;
    private static final int KEY_LARGER = 61;
    private static final double MEASURE_FOV = 30.0;
    private static final double MEASURE_SENSITIVITY = 0.0;
    private static final double TRAVEL_FOV = 110.0;
    private static final double TRAVEL_SENSITIVITY = 0.4;
    private static final double MEASURE_SENSITIVITY_THRESHOLD = 0.25;
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
    private static final int PANEL_BACKGROUND = 0x1C1F24;
    private static final int TITLE_BAR_BACKGROUND = 0x2C3138;
    private static final int SEPARATOR = 0x3A3F47;
    private static final int COLOR_TITLE = 0xF0F2F5;
    private static final int COLOR_VERSION = 0x7A828C;
    private static final int COLOR_HEADER = 0xB4BECC;
    private static final int COLOR_COORD = 0x8FA8C8;
    private static final int COLOR_NUMBER = 0xD8DDE2;
    private static final int COLOR_HIGH = 0x5BC46B;
    private static final int COLOR_MEDIUM = 0xE0A83C;
    private static final int COLOR_LOW = 0xC24A4A;
    private static final int COLOR_NOTICE = 0xFFD24A;
    private static final int COLOR_WARNING = 0xFF7A5A;
    private static final double MIN_USEFUL_BASELINE = 100.0;
    private static final double PERCENT_HIGH = 50.0;
    private static final double PERCENT_MEDIUM = 5.0;
    /** Above this, one throw is enough and the "walk and throw again" advice is hidden. */
    private static final double CONFIDENT = 0.95;
    /**
     * A boat-eye throw is lined up to within about half a pixel, and judging
     * the middle of the eye's sprite adds roughly as much again, so its error
     * is never taken as less than this many pixels, whatever the setting says.
     */
    private static final double MIN_BOAT_SIGMA_PIXELS = 0.6;
    private static final long NOTICE_DURATION_MS = 3000L;
    private static final int MAX_MEASUREMENTS = 3;
    private static final String VERSION = "v7.0.0";
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

    /** Once a frame, from GameRenderer.render - so it runs with the HUD hidden too. */
    public static void frame(MinecraftClient client) {
        if (client == null || client.player == null) {
            return;
        }
        ToolscreenBridge.registerSidePanel(StrongholdOverlay::renderSidePanel);
        EyeTracker.tick(client);
        BlindPractice.tick(client);
        StrongholdOverlay.pollKeys(client);
    }

    /** The normal HUD panel, top left. */
    public static void render(MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }
        // In a Toolscreen mode the panel is drawn beside the strip instead: here
        // it would land in the part of the tall render that is cropped away.
        if (StrongholdOverlay.inSidePanel()) {
            return;
        }
        float scale = StrongholdConfig.scaleFactor();
        matrices.push();
        matrices.scale(scale, scale, 1.0f);
        try {
            StrongholdOverlay.draw(client, matrices);
        }
        finally {
            matrices.pop();
        }
    }

    private static boolean inSidePanel() {
        return ToolscreenBridge.sidePanelRegistered() && ToolscreenBridge.isOverrideActive();
    }

    /**
     * Draws the panel into Toolscreen Mobile's letterbox. {@code box} is
     * {x, y, width, height} in real screen pixels.
     */
    private static void renderSidePanel(MatrixStack matrices, int[] box) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || !StrongholdOverlay.hasContent() && notice == null) {
            return;
        }
        double scale = client.getWindow().getScaleFactor() * StrongholdConfig.scaleFactor();
        double fitWidth = (double)box[2] / (double)(PANEL_X + PANEL_WIDTH + PANEL_X);
        double fitHeight = (double)box[3] / (double)(PANEL_Y + StrongholdOverlay.panelHeight() + 4 + ROW_HEIGHT);
        scale = Math.min(scale, Math.min(fitWidth, fitHeight));
        if (scale <= 0.0) {
            return;
        }
        matrices.push();
        matrices.translate((double)box[0], (double)box[1], 0.0);
        matrices.scale((float)scale, (float)scale, 1.0f);
        try {
            StrongholdOverlay.draw(client, matrices);
        }
        finally {
            matrices.pop();
        }
    }

    private static void pollKeys(MinecraftClient client) {
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
        boolean modifier = InputUtil.isKeyPressed(window, KEY_MODIFIER);
        boolean capture = modifier && InputUtil.isKeyPressed(window, KEY_CAPTURE);
        boolean toggle = !modifier && InputUtil.isKeyPressed(window, KEY_TOGGLE);
        boolean settings = !modifier && InputUtil.isKeyPressed(window, KEY_SETTINGS);
        boolean measureMode = !modifier && InputUtil.isKeyPressed(window, KEY_MEASURE_MODE);
        boolean calibrate = !modifier && InputUtil.isKeyPressed(window, KEY_CALIBRATE);
        boolean teleport = !modifier && InputUtil.isKeyPressed(window, KEY_TELEPORT);
        boolean tallScreen = !modifier && InputUtil.isKeyPressed(window, KEY_TALL_SCREEN);
        boolean pixelMinus = !modifier && InputUtil.isKeyPressed(window, KEY_PIXEL_MINUS);
        boolean pixelPlus = !modifier && InputUtil.isKeyPressed(window, KEY_PIXEL_PLUS);
        boolean reset = !modifier && InputUtil.isKeyPressed(window, KEY_RESET);
        boolean fade = !modifier && InputUtil.isKeyPressed(window, KEY_FADE);
        boolean brighten = !modifier && InputUtil.isKeyPressed(window, KEY_BRIGHTEN);
        boolean smaller = !modifier && InputUtil.isKeyPressed(window, KEY_SMALLER);
        boolean larger = !modifier && InputUtil.isKeyPressed(window, KEY_LARGER);
        if (capture && !prevCapture) {
            StrongholdOverlay.capture(client, client.player);
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
            StrongholdOverlay.adjustLastAngle(-1);
        }
        if (pixelPlus && !prevPixelPlus) {
            StrongholdOverlay.adjustLastAngle(1);
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
        GameOptions options = client.options;
        if (options == null) {
            return;
        }
        boolean measuring = options.mouseSensitivity <= MEASURE_SENSITIVITY_THRESHOLD;
        if (measuring) {
            options.fov = TRAVEL_FOV;
            options.mouseSensitivity = TRAVEL_SENSITIVITY;
            StrongholdOverlay.showNotice("Travel: FOV 110, sens 80%");
        } else {
            options.fov = MEASURE_FOV;
            options.mouseSensitivity = MEASURE_SENSITIVITY;
            StrongholdOverlay.showNotice("Measure: FOV 30, slow mouse");
        }
        options.write();
    }

    /**
     * Moves the last throw's angle by whole pixels, for an eye that is not
     * exactly under the crosshair: if it sits 3 ruler cells right of the
     * centre line, press the right arrow 3 times.
     */
    private static void adjustLastAngle(int increments) {
        if (MEASUREMENTS.isEmpty()) {
            StrongholdOverlay.showNotice("No throw to adjust");
            return;
        }
        EyeMeasurement last = MEASUREMENTS.get(MEASUREMENTS.size() - 1);
        if (last.fromEyeFlight) {
            StrongholdOverlay.showNotice("Tracked throws need no adjusting");
            return;
        }
        last.yaw = (float)((double)last.yaw + (double)increments * last.yawPerPixel);
        last.pixelAdjustments += increments;
        StrongholdOverlay.showNotice("Angle " + StrongholdOverlay.signed(last.pixelAdjustments) + " px (" + StrongholdOverlay.decimals(last.yawPerPixel * (double)last.pixelAdjustments, 4) + " deg)");
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
        player.sendChatMessage("/tp @s " + best.overworldX + " " + Math.round(player.getY()) + " " + best.overworldZ);
        StrongholdOverlay.showNotice("Teleporting to " + best.overworldX + ", " + best.overworldZ);
    }

    /** Stand in the stronghold's chunk and press K: each throw's error trains the aim error setting. */
    private static void calibrateHere(ClientPlayerEntity player) {
        if (MEASUREMENTS.isEmpty()) {
            StrongholdOverlay.showNotice("No throws to calibrate from");
            return;
        }
        int chunkX = Math.floorDiv((int)Math.floor(player.getX()), 16);
        int chunkZ = Math.floorDiv((int)Math.floor(player.getZ()), 16);
        double targetX = chunkX * 16 + StrongholdPrediction.EYE_TARGET_IN_CHUNK;
        double targetZ = chunkZ * 16 + StrongholdPrediction.EYE_TARGET_IN_CHUNK;
        List<Double> errors = new ArrayList<Double>();
        for (EyeMeasurement measurement : MEASUREMENTS) {
            // Boat-eye and tracked throws have their own error; mixing them in
            // would drag the normal aim error down.
            if (measurement.fromBoat || measurement.fromEyeFlight) continue;
            double predicted = Math.toDegrees(Math.atan2(-(targetX - measurement.x), targetZ - measurement.z));
            errors.add(StrongholdOverlay.wrapYaw(predicted - (double)measurement.yaw));
        }
        if (errors.isEmpty()) {
            StrongholdOverlay.showNotice("Only boat eye throws - nothing to calibrate");
            return;
        }
        double[] values = new double[errors.size()];
        for (int i = 0; i < values.length; ++i) {
            values[i] = errors.get(i);
        }
        double sigma = StrongholdConfig.addCalibrationErrors(values);
        if (sigma < 0.0) {
            StrongholdOverlay.showNotice("Calibration failed");
            return;
        }
        StrongholdOverlay.showNotice("Calibrated: aim error " + StrongholdOverlay.decimals(sigma, 2) + " from " + StrongholdConfig.calibrationThrows() + " throws");
    }

    private static void capture(MinecraftClient client, ClientPlayerEntity player) {
        boolean useTracking = StrongholdConfig.trackEyeFlight();
        if (useTracking && EyeTracker.hasBearing() && EyeTracker.isCaptured()) {
            StrongholdOverlay.showNotice("That eye is already added");
            return;
        }
        float yaw = (float)((double)player.yaw - PixelPerfect.packetRoundingCorrection(player.yaw));
        double yawPerPixel = PixelPerfect.yawDegreesPerPixel(client, player.pitch);
        EyeMeasurement measurement;
        String detail;
        if (useTracking && EyeTracker.hasBearing()) {
            measurement = new EyeMeasurement(EyeTracker.originX(), EyeTracker.originZ(), (float)EyeTracker.bearingDegrees(), EyeTracker.TRACKED_SIGMA, true);
            detail = " tracked over " + StrongholdOverlay.decimals(EyeTracker.travelled(), 0) + " blocks";
        } else if (StrongholdConfig.boatEye() && PixelPerfect.zoomedForBoatEye(client)) {
            // Boat eye. Ninjabrain Bot needs the boat to recover the angle
            // decimals F3+C leaves out; this mod reads the exact angle from the
            // game, so the precision here comes from lining the eye up to the
            // pixel on a magnified view, then correcting with the arrow keys.
            double sigma = Math.max(StrongholdConfig.boatEyeSigma(), MIN_BOAT_SIGMA_PIXELS * yawPerPixel);
            measurement = new EyeMeasurement(player.getX(), player.getZ(), yaw, sigma, false);
            measurement.fromBoat = true;
            detail = " boat eye - count the ruler cells, arrows to correct";
        } else {
            measurement = new EyeMeasurement(player.getX(), player.getZ(), yaw, StrongholdConfig.angleSigma(), false);
            detail = StrongholdConfig.boatEye()
                    ? " not zoomed in - normal aim error (up arrow: Eye Measure)"
                    : " from crosshair";
        }
        measurement.pitch = player.pitch;
        measurement.yawPerPixel = yawPerPixel;
        if (StrongholdConfig.measuringTrainer() && EyeTracker.hasBearing()) {
            double error = StrongholdOverlay.wrapYaw((double)measurement.yaw - EyeTracker.bearingDegrees());
            boatScore = "Last throw off by " + StrongholdOverlay.decimals(Math.abs(error), 4) + " deg (" + StrongholdOverlay.decimals(error / yawPerPixel, 1) + " px)";
        }
        EyeTracker.markCaptured();
        StrongholdOverlay.record(measurement, detail);
    }

    private static void record(EyeMeasurement measurement, String detail) {
        MEASUREMENTS.add(measurement);
        hidden = false;
        while (MEASUREMENTS.size() > MAX_MEASUREMENTS) {
            MEASUREMENTS.remove(0);
        }
        StrongholdOverlay.calculate();
        StrongholdOverlay.showNotice("Eye " + MEASUREMENTS.size() + detail);
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
        boatScore = null;
        hidden = false;
        EyeTracker.forget();
    }

    public static void notice(String text) {
        StrongholdOverlay.showNotice(text);
    }

    private static void showNotice(String text) {
        notice = text;
        noticeExpiresAt = System.currentTimeMillis() + NOTICE_DURATION_MS;
    }

    private static boolean hasContent() {
        return !hidden && (!MEASUREMENTS.isEmpty() || StrongholdConfig.trackEyeFlight() && EyeTracker.isReady() || results != null && !results.isEmpty());
    }

    private static void draw(MinecraftClient client, MatrixStack matrices) {
        boolean hasContent = StrongholdOverlay.hasContent();
        if (hasContent) {
            StrongholdOverlay.drawPanel(client, matrices);
        }
        if (notice != null) {
            if (System.currentTimeMillis() > noticeExpiresAt) {
                notice = null;
            } else {
                int y = hasContent ? PANEL_Y + StrongholdOverlay.panelHeight() + 4 : PANEL_Y;
                StrongholdOverlay.text(client, matrices, notice, PANEL_X + PADDING, y, COLOR_NOTICE);
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
        return results == null ? 0 : results.size();
    }

    /**
     * Whether to show "walk and throw again". Only while one throw is not
     * enough - a good boat eye throw usually is, and then it is just noise.
     */
    private static boolean showAdvice() {
        if (advice == null || MEASUREMENTS.size() != 1) {
            return false;
        }
        return results == null || results.isEmpty() || !results.get(0).hasCertainty() || results.get(0).certainty < CONFIDENT;
    }

    private static int trainerLines() {
        int lines = 3;
        if (!MEASUREMENTS.isEmpty()) {
            ++lines;
        }
        if (boatScore != null) {
            ++lines;
        }
        return lines;
    }

    private static int panelHeight() {
        int height = TITLE_BAR_HEIGHT;
        if (StrongholdOverlay.candidateRows() > 0) {
            height += 3 + ROW_HEIGHT + StrongholdOverlay.candidateRows() * ROW_HEIGHT + 4;
        }
        height += 1 + 3 + ROW_HEIGHT + ROW_HEIGHT;
        height += MEASUREMENTS.size() * ROW_HEIGHT;
        if (StrongholdOverlay.showAdvice()) {
            height += ROW_HEIGHT * StrongholdOverlay.adviceLineCount() + 2;
        }
        if (StrongholdConfig.measuringTrainer()) {
            height += ROW_HEIGHT * StrongholdOverlay.trainerLines() + 2;
        }
        if (StrongholdConfig.trackEyeFlight() && EyeTracker.isReady()) {
            height += 13;
        }
        if (StrongholdOverlay.warning() != null) {
            height += 13;
        }
        return height + PADDING;
    }

    private static void drawPanel(MinecraftClient client, MatrixStack matrices) {
        int left = PANEL_X;
        int top = PANEL_Y;
        int right = left + PANEL_WIDTH;
        int bottom = top + StrongholdOverlay.panelHeight();
        StrongholdOverlay.roundedRect(matrices, left, top, right, bottom, PANEL_BACKGROUND);
        StrongholdOverlay.roundedTop(matrices, left, top, right, top + TITLE_BAR_HEIGHT, TITLE_BAR_BACKGROUND);
        int y = top + 3;
        StrongholdOverlay.text(client, matrices, "Stronghold Finder", left + PADDING, y, COLOR_TITLE);
        int titleWidth = client.textRenderer.getWidth("Stronghold Finder");
        StrongholdOverlay.text(client, matrices, VERSION, left + PADDING + titleWidth + 4, y, COLOR_VERSION);
        y = top + TITLE_BAR_HEIGHT;
        if (StrongholdOverlay.candidateRows() > 0) {
            y += 3;
            StrongholdOverlay.centred(client, matrices, "Overworld", left + COL_COORD_CENTRE, y, COLOR_HEADER);
            StrongholdOverlay.rightAligned(client, matrices, "%", left + COL_PERCENT_RIGHT, y, COLOR_HEADER);
            StrongholdOverlay.rightAligned(client, matrices, "Dist.", left + COL_DIST_RIGHT, y, COLOR_HEADER);
            StrongholdOverlay.centred(client, matrices, "Nether", left + COL_NETHER_CENTRE, y, COLOR_HEADER);
            y += ROW_HEIGHT;
            for (StrongholdPrediction candidate : results) {
                String overworld = "(" + candidate.overworldX + ", " + candidate.overworldZ + ")";
                String nether = "(" + candidate.netherX() + ", " + candidate.netherZ() + ")";
                double dx = (double)candidate.overworldX - client.player.getX();
                double dz = (double)candidate.overworldZ - client.player.getZ();
                long distance = Math.round(Math.sqrt(dx * dx + dz * dz));
                StrongholdOverlay.centred(client, matrices, overworld, left + COL_COORD_CENTRE, y, COLOR_COORD);
                if (candidate.hasCertainty()) {
                    double percent = candidate.certainty * 100.0;
                    StrongholdOverlay.rightAligned(client, matrices, StrongholdOverlay.decimals(percent, 1) + "%", left + COL_PERCENT_RIGHT, y, StrongholdOverlay.percentColor(percent));
                } else {
                    StrongholdOverlay.rightAligned(client, matrices, "-", left + COL_PERCENT_RIGHT, y, COLOR_NUMBER);
                }
                StrongholdOverlay.rightAligned(client, matrices, Long.toString(distance), left + COL_DIST_RIGHT, y, COLOR_NUMBER);
                StrongholdOverlay.centred(client, matrices, nether, left + COL_NETHER_CENTRE, y, COLOR_COORD);
                y += ROW_HEIGHT;
            }
            y += 4;
        }
        StrongholdOverlay.fill(matrices, left + PADDING, y, right - PADDING, y + 1, SEPARATOR);
        y += 4;
        StrongholdOverlay.text(client, matrices, "Ender eye throws", left + PADDING, y, COLOR_TITLE);
        y += ROW_HEIGHT;
        StrongholdOverlay.text(client, matrices, "Src", left + PADDING, y, COLOR_HEADER);
        StrongholdOverlay.rightAligned(client, matrices, "x", left + COL_THROW_X_RIGHT, y, COLOR_HEADER);
        StrongholdOverlay.rightAligned(client, matrices, "z", left + COL_THROW_Z_RIGHT, y, COLOR_HEADER);
        StrongholdOverlay.rightAligned(client, matrices, "Angle", left + COL_THROW_ANGLE_RIGHT, y, COLOR_HEADER);
        StrongholdOverlay.rightAligned(client, matrices, "Error", left + COL_THROW_ERROR_RIGHT, y, COLOR_HEADER);
        y += ROW_HEIGHT;
        StrongholdPrediction best = results == null || results.isEmpty() ? null : results.get(0);
        for (EyeMeasurement measurement : MEASUREMENTS) {
            String source = measurement.fromEyeFlight ? "eye" : (measurement.fromBoat ? "boat" : "aim");
            int sourceColor = measurement.fromEyeFlight || measurement.fromBoat ? COLOR_HIGH : COLOR_LOW;
            boolean precise = measurement.fromEyeFlight || measurement.fromBoat;
            StrongholdOverlay.text(client, matrices, source, left + PADDING, y, sourceColor);
            StrongholdOverlay.rightAligned(client, matrices, StrongholdOverlay.decimals(measurement.x, 2), left + COL_THROW_X_RIGHT, y, COLOR_COORD);
            StrongholdOverlay.rightAligned(client, matrices, StrongholdOverlay.decimals(measurement.z, 2), left + COL_THROW_Z_RIGHT, y, COLOR_COORD);
            StrongholdOverlay.rightAligned(client, matrices, StrongholdOverlay.decimals(StrongholdOverlay.wrapYaw(measurement.yaw), precise ? 3 : 2), left + COL_THROW_ANGLE_RIGHT, y, precise ? COLOR_HIGH : COLOR_NUMBER);
            if (best == null) {
                StrongholdOverlay.rightAligned(client, matrices, "-", left + COL_THROW_ERROR_RIGHT, y, COLOR_NUMBER);
            } else {
                double error = StrongholdOverlay.angleError(measurement, best);
                StrongholdOverlay.rightAligned(client, matrices, StrongholdOverlay.decimals(error, precise ? 4 : 3), left + COL_THROW_ERROR_RIGHT, y, StrongholdOverlay.errorColor(error, measurement.sigma));
            }
            y += ROW_HEIGHT;
        }
        if (StrongholdOverlay.showAdvice()) {
            y += 2;
            for (String line : advice.split("\n")) {
                StrongholdOverlay.text(client, matrices, line, left + PADDING, y, COLOR_HEADER);
                y += ROW_HEIGHT;
            }
        }
        if (StrongholdConfig.measuringTrainer()) {
            y += 2;
            double perPixel = PixelPerfect.degreesPerPixel(client);
            boolean zoomed = PixelPerfect.zoomedForBoatEye(client);
            boolean corner = PixelPerfect.isOnCorner(client.player.getX(), client.player.getZ());
            StrongholdOverlay.text(client, matrices, "FOV " + StrongholdOverlay.decimals(PixelPerfect.renderedFov(client), 1) + ", render " + client.getWindow().getFramebufferHeight() + " px tall", left + PADDING, y, COLOR_HEADER);
            y += ROW_HEIGHT;
            StrongholdOverlay.text(client, matrices, "1 px = " + StrongholdOverlay.decimals(perPixel, 5) + " deg" + (zoomed ? "  boat eye ready" : "  zoom in (up arrow)"), left + PADDING, y, zoomed ? COLOR_HIGH : COLOR_MEDIUM);
            y += ROW_HEIGHT;
            StrongholdOverlay.text(client, matrices, corner ? "On a block corner - position is exact" : "Not on a corner - stand against a block", left + PADDING, y, corner ? COLOR_HIGH : COLOR_MEDIUM);
            y += ROW_HEIGHT;
            if (!MEASUREMENTS.isEmpty()) {
                EyeMeasurement last = MEASUREMENTS.get(MEASUREMENTS.size() - 1);
                StrongholdOverlay.text(client, matrices, "Last throw " + StrongholdOverlay.signed(last.pixelAdjustments) + " px  (arrows adjust)", left + PADDING, y, COLOR_NUMBER);
                y += ROW_HEIGHT;
            }
            if (boatScore != null) {
                StrongholdOverlay.text(client, matrices, boatScore, left + PADDING, y, COLOR_NUMBER);
                y += ROW_HEIGHT;
            }
        }
        if (StrongholdConfig.trackEyeFlight() && EyeTracker.isReady()) {
            y += 2;
            StrongholdOverlay.text(client, matrices, "Eye tracked - F3+C to add it", left + PADDING, y, COLOR_HIGH);
            y += ROW_HEIGHT;
        }
        String warning = StrongholdOverlay.warning();
        if (warning != null) {
            y += 2;
            StrongholdOverlay.text(client, matrices, warning, left + PADDING, y, COLOR_WARNING);
        }
    }

    private static String warning() {
        if (MEASUREMENTS.isEmpty() || results == null || results.isEmpty()) {
            return null;
        }
        StrongholdPrediction best = results.get(0);
        if (MEASUREMENTS.size() >= 2 && (!best.hasCertainty() || best.certainty < CONFIDENT)) {
            double baseline = StrongholdOverlay.longestBaseline();
            if (baseline < MIN_USEFUL_BASELINE) {
                return "Throws " + Math.round(baseline) + " blocks apart - move further";
            }
        }
        if (MEASUREMENTS.size() < 2) {
            return null;
        }
        double worst = 0.0;
        double chiSquare = 0.0;
        for (EyeMeasurement measurement : MEASUREMENTS) {
            double error = StrongholdOverlay.angleError(measurement, best);
            double scaled = error / measurement.sigma;
            chiSquare += scaled * scaled;
            worst = Math.max(worst, Math.abs(error));
        }
        if (chiSquare > 2.0 * (double)MEASUREMENTS.size()) {
            return "Angles off by " + StrongholdOverlay.decimals(worst, 3) + " deg - retake";
        }
        return null;
    }

    private static double longestBaseline() {
        double longest = 0.0;
        for (int i = 0; i < MEASUREMENTS.size(); ++i) {
            for (int j = i + 1; j < MEASUREMENTS.size(); ++j) {
                EyeMeasurement a = MEASUREMENTS.get(i);
                EyeMeasurement b = MEASUREMENTS.get(j);
                longest = Math.max(longest, Math.hypot(a.x - b.x, a.z - b.z));
            }
        }
        return longest;
    }

    /** How far a throw's angle is from pointing at the prediction's eye target, (8, 8) in its chunk. */
    private static double angleError(EyeMeasurement measurement, StrongholdPrediction prediction) {
        double predicted = Math.toDegrees(Math.atan2(-(prediction.targetX() - measurement.x), prediction.targetZ() - measurement.z));
        return StrongholdOverlay.wrapYaw(predicted - (double)measurement.yaw);
    }

    private static int percentColor(double percent) {
        if (percent >= PERCENT_HIGH) {
            return COLOR_HIGH;
        }
        if (percent >= PERCENT_MEDIUM) {
            return COLOR_MEDIUM;
        }
        return COLOR_LOW;
    }

    /** Green within one aim error, amber within three, red beyond. */
    private static int errorColor(double error, double sigma) {
        double magnitude = Math.abs(error);
        if (magnitude <= sigma) {
            return COLOR_HIGH;
        }
        if (magnitude <= 3.0 * sigma) {
            return COLOR_MEDIUM;
        }
        return COLOR_LOW;
    }

    private static void fill(MatrixStack matrices, int x1, int y1, int x2, int y2, int rgb) {
        DrawableHelper.fill(matrices, x1, y1, x2, y2, StrongholdConfig.applyOpacity(rgb));
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

    private static String signed(int value) {
        return (value >= 0 ? "+" : "") + value;
    }

    private static String decimals(double value, int places) {
        long scale = 1L;
        for (int i = 0; i < places; ++i) {
            scale *= 10L;
        }
        long scaled = Math.round(value * (double)scale);
        boolean negative = scaled < 0L;
        if (negative) {
            scaled = -scaled;
        }
        String fraction = Long.toString(scaled % scale);
        while (fraction.length() < places) {
            fraction = "0" + fraction;
        }
        String text = places == 0 ? Long.toString(scaled / scale) : scaled / scale + "." + fraction;
        return negative ? "-" + text : text;
    }

    public static String calculatorName() {
        return CALCULATOR.name();
    }
}
