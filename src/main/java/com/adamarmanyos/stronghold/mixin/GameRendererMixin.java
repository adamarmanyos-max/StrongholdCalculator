package com.adamarmanyos.stronghold.mixin;

import com.adamarmanyos.stronghold.StrongholdOverlay;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Runs the per-frame work - hotkeys, eye tracking, blind practice - from
 * {@code GameRenderer.render} rather than the HUD.
 *
 * <p>Minecraft skips {@code InGameHud.render} entirely while the HUD is hidden
 * with F1, which is the natural thing to do while measuring, so hotkeys polled
 * from there stopped working exactly when they were needed.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "render(FJZ)V", at = @At("TAIL"))
    private void strongholdfinder$frame(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        StrongholdOverlay.frame(MinecraftClient.getInstance());
    }
}
