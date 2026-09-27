package com.adamarmanyos.stronghold.mixin;

import com.adamarmanyos.stronghold.StrongholdOverlay;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={InGameHud.class})
public class InGameHudMixin {
    @Inject(method={"render(Lnet/minecraft/client/util/math/MatrixStack;F)V"}, at={@At(value="TAIL")})
    private void strongholdfinder$renderOverlay(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        StrongholdOverlay.render(matrices);
    }
}

