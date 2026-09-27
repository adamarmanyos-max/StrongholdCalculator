package com.adamarmanyos.stronghold;

import com.adamarmanyos.stronghold.BlindPractice;
import com.adamarmanyos.stronghold.StrongholdConfig;
import net.minecraft.text.Text;
import net.minecraft.text.LiteralText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

public class StrongholdSettingsScreen
extends Screen {
    private static final int PANEL_WIDTH = 220;
    private static final int PANEL_HEIGHT = 298;
    private static final int SLIDER_WIDTH = 180;
    private static final int SLIDER_HEIGHT = 16;
    private static final int KNOB_WIDTH = 8;
    private static final int BACKGROUND = -400810204;
    private static final int BORDER = -12959929;
    private static final int TITLE_BAR = -13881032;
    private static final int TRACK = -15460581;
    private static final int TRACK_FILL = -12821888;
    private static final int KNOB = -4931892;
    private static final int KNOB_ACTIVE = -1512208;
    private static final int TEXT = -986379;
    private static final int TEXT_DIM = -7695716;
    private int panelLeft;
    private int panelTop;
    private int sizeSliderY;
    private int opacitySliderY;
    private int sigmaSliderY;
    private int toggleY;
    private int calibrationY;
    private int practiceY;
    private int boatY;
    private int trainerRowY;
    private int dragging = -1;

    public StrongholdSettingsScreen() {
        super(new LiteralText("Stronghold Finder Settings"));
    }

    protected void init() {
        this.panelLeft = (this.width - 220) / 2;
        this.panelTop = (this.height - 298) / 2;
        this.sizeSliderY = this.panelTop + 38;
        this.opacitySliderY = this.panelTop + 74;
        this.sigmaSliderY = this.panelTop + 110;
        this.toggleY = this.panelTop + 140;
        this.calibrationY = this.panelTop + 168;
        this.practiceY = this.panelTop + 196;
        this.boatY = this.panelTop + 224;
        this.trainerRowY = this.panelTop + 252;
    }

    public boolean isPauseScreen() {
        return false;
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        int left = this.panelLeft;
        int top = this.panelTop;
        int right = left + 220;
        int bottom = top + 298;
        StrongholdSettingsScreen.roundedRect(matrices, left, top, right, bottom, -12959929);
        StrongholdSettingsScreen.roundedRect(matrices, left + 1, top + 1, right - 1, bottom - 1, -400810204);
        StrongholdSettingsScreen.roundedTop(matrices, left + 1, top + 1, right - 1, top + 20, -13881032);
        this.centeredText(matrices, "Stronghold Finder", left + 110, top + 7, -986379);
        this.drawSlider(matrices, "Size", StrongholdConfig.scalePercent() + "%", this.sizeFraction(), this.sizeSliderY, this.dragging == 0);
        this.drawSlider(matrices, "Opacity", StrongholdConfig.opacityPercent() + "%", this.opacityFraction(), this.opacitySliderY, this.dragging == 1);
        this.drawSlider(matrices, "Aim error", this.sigmaText(), this.sigmaFraction(), this.sigmaSliderY, this.dragging == 2);
        this.drawToggle(matrices, "Measure from", StrongholdConfig.trackEyeFlight() ? "eye flight" : "my crosshair", this.toggleY);
        int samples = StrongholdConfig.calibrationThrows();
        this.drawToggle(matrices, "Calibration", (String)(samples == 0 ? "none - press K" : samples + " throws (reset)"), this.calibrationY);
        this.drawToggle(matrices, "Blind practice", "start (needs cheats)", this.practiceY);
        this.drawToggle(matrices, "Boat eye (pixel perfect)", StrongholdConfig.boatEye() ? "on" : "off", this.boatY);
        this.drawToggle(matrices, "Measuring trainer", StrongholdConfig.measuringTrainer() ? "on" : "off", this.trainerRowY);
        this.centeredText(matrices, "Esc to close", left + 110, bottom - 14, -7695716);
        super.render(matrices, mouseX, mouseY, delta);
    }

    private static void roundedRect(MatrixStack matrices, int left, int top, int right, int bottom, int color) {
        DrawableHelper.fill(matrices, (int)(left + 3), (int)top, (int)(right - 3), (int)(top + 1), (int)color);
        DrawableHelper.fill(matrices, (int)(left + 1), (int)(top + 1), (int)(right - 1), (int)(top + 3), (int)color);
        DrawableHelper.fill(matrices, (int)left, (int)(top + 3), (int)right, (int)(bottom - 3), (int)color);
        DrawableHelper.fill(matrices, (int)(left + 1), (int)(bottom - 3), (int)(right - 1), (int)(bottom - 1), (int)color);
        DrawableHelper.fill(matrices, (int)(left + 3), (int)(bottom - 1), (int)(right - 3), (int)bottom, (int)color);
    }

    private static void roundedTop(MatrixStack matrices, int left, int top, int right, int bottom, int color) {
        DrawableHelper.fill(matrices, (int)(left + 3), (int)top, (int)(right - 3), (int)(top + 1), (int)color);
        DrawableHelper.fill(matrices, (int)(left + 1), (int)(top + 1), (int)(right - 1), (int)(top + 3), (int)color);
        DrawableHelper.fill(matrices, (int)left, (int)(top + 3), (int)right, (int)bottom, (int)color);
    }

    private void centeredText(MatrixStack matrices, String value, int centreX, int y, int color) {
        int width = this.textRenderer.getWidth(value);
        this.textRenderer.draw(matrices, value, (float)centreX - (float)width / 2.0f, (float)y, color);
    }

    private void drawToggle(MatrixStack matrices, String label, String value, int y) {
        int left = this.panelLeft + 20;
        this.textRenderer.draw(matrices, label, (float)left, (float)(y + 4), -986379);
        int boxWidth = 96;
        int boxLeft = left + 180 - boxWidth;
        DrawableHelper.fill(matrices, (int)boxLeft, (int)y, (int)(boxLeft + boxWidth), (int)(y + 16), (int)-15460581);
        int width = this.textRenderer.getWidth(value);
        this.textRenderer.draw(matrices, value, (float)boxLeft + (float)(boxWidth - width) / 2.0f, (float)(y + 4), -986379);
    }

    private void drawSlider(MatrixStack matrices, String label, String value, double fraction, int y, boolean active) {
        int left = this.panelLeft + 20;
        this.textRenderer.draw(matrices, label, (float)left, (float)(y - 11), -986379);
        int valueWidth = this.textRenderer.getWidth(value);
        this.textRenderer.draw(matrices, value, (float)(left + 180 - valueWidth), (float)(y - 11), -7695716);
        DrawableHelper.fill(matrices, (int)left, (int)y, (int)(left + 180), (int)(y + 16), (int)-15460581);
        int fillWidth = (int)Math.round(fraction * 172.0);
        DrawableHelper.fill(matrices, (int)left, (int)y, (int)(left + fillWidth + 4), (int)(y + 16), (int)-12821888);
        int knobLeft = left + fillWidth;
        DrawableHelper.fill(matrices, (int)knobLeft, (int)(y - 1), (int)(knobLeft + 8), (int)(y + 16 + 1), (int)(active ? -1512208 : -4931892));
    }

    private double sizeFraction() {
        int steps = StrongholdConfig.SCALE_STEPS.length - 1;
        return steps <= 0 ? 0.0 : (double)StrongholdConfig.scaleIndex() / (double)steps;
    }

    private double opacityFraction() {
        double span = 90.0;
        return (double)(StrongholdConfig.opacityPercent() - 10) / span;
    }

    private String sigmaText() {
        int hundredths = StrongholdConfig.sigmaHundredths();
        Object fraction = hundredths < 10 ? "0" + hundredths : Integer.toString(hundredths);
        return "0." + (String)fraction + " deg";
    }

    private double sigmaFraction() {
        double span = 48.0;
        return (double)(StrongholdConfig.sigmaHundredths() - 2) / span;
    }

    private void applySigma(double fraction) {
        double span = 48.0;
        StrongholdConfig.setSigmaHundredths((int)Math.round(2.0 + fraction * span));
    }

    private void applySize(double fraction) {
        int steps = StrongholdConfig.SCALE_STEPS.length - 1;
        StrongholdConfig.setScaleIndex((int)Math.round(fraction * (double)steps));
    }

    private void applyOpacity(double fraction) {
        double span = 90.0;
        StrongholdConfig.setOpacityPercent((int)Math.round(10.0 + fraction * span));
    }

    private int sliderAt(double mouseX, double mouseY) {
        int left = this.panelLeft + 20;
        if (mouseX < (double)(left - 8) || mouseX > (double)(left + 180 + 8)) {
            return -1;
        }
        if (mouseY >= (double)(this.sizeSliderY - 2) && mouseY <= (double)(this.sizeSliderY + 16 + 2)) {
            return 0;
        }
        if (mouseY >= (double)(this.opacitySliderY - 2) && mouseY <= (double)(this.opacitySliderY + 16 + 2)) {
            return 1;
        }
        if (mouseY >= (double)(this.sigmaSliderY - 2) && mouseY <= (double)(this.sigmaSliderY + 16 + 2)) {
            return 2;
        }
        return -1;
    }

    private double fractionAt(double mouseX) {
        int left = this.panelLeft + 20;
        double fraction = (mouseX - (double)left - 4.0) / 172.0;
        if (fraction < 0.0) {
            return 0.0;
        }
        if (fraction > 1.0) {
            return 1.0;
        }
        return fraction;
    }

    private boolean isOnRow(double mouseX, double mouseY, int rowY) {
        int left = this.panelLeft + 20;
        return mouseX >= (double)left && mouseX <= (double)(left + 180) && mouseY >= (double)rowY && mouseY <= (double)(rowY + 16);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isOnRow(mouseX, mouseY, this.toggleY)) {
            StrongholdConfig.setTrackEyeFlight(!StrongholdConfig.trackEyeFlight());
            return true;
        }
        if (this.isOnRow(mouseX, mouseY, this.calibrationY)) {
            StrongholdConfig.resetCalibration();
            return true;
        }
        if (this.isOnRow(mouseX, mouseY, this.boatY)) {
            StrongholdConfig.setBoatEye(!StrongholdConfig.boatEye());
            return true;
        }
        if (this.isOnRow(mouseX, mouseY, this.trainerRowY)) {
            StrongholdConfig.setMeasuringTrainer(!StrongholdConfig.measuringTrainer());
            return true;
        }
        if (this.isOnRow(mouseX, mouseY, this.practiceY)) {
            MinecraftClient screenClient = MinecraftClient.getInstance();
            screenClient.openScreen(null);
            BlindPractice.start(screenClient);
            return true;
        }
        int slider = this.sliderAt(mouseX, mouseY);
        if (slider >= 0) {
            this.dragging = slider;
            this.apply(slider, this.fractionAt(mouseX));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.dragging >= 0) {
            this.apply(this.dragging, this.fractionAt(mouseX));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.dragging = -1;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void apply(int slider, double fraction) {
        if (slider == 0) {
            this.applySize(fraction);
        } else if (slider == 1) {
            this.applyOpacity(fraction);
        } else {
            this.applySigma(fraction);
        }
    }
}

