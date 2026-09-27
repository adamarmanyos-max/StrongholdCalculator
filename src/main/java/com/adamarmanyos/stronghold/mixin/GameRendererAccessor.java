package com.adamarmanyos.stronghold.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Calls the private {@code GameRenderer.getFov}. Going through the real method
 * means other mods' changes to it apply - Toolscreen Mobile halves it in Eye
 * Measure - so the result is the FOV actually on screen.
 */
@Mixin(GameRenderer.class)
public interface GameRendererAccessor {
    @Invoker("getFov")
    double strongholdfinder$getFov(Camera camera, float tickDelta, boolean changingFov);
}
