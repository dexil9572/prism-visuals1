package ru.prism.vis.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.prism.vis.module.ModuleManager;

/** No Hurt Cam: отменяем наклон камеры при получении урона. */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void prism$noHurtCam(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (ModuleManager.NO_HURT_CAM.isEnabled()) {
            ci.cancel();
        }
    }
}
