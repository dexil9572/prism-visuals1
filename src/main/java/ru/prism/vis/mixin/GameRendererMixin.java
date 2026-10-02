package ru.prism.vis.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.prism.vis.module.ModuleManager;

/** No Hurt Cam: отменяем наклон камеры при получении урона. */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void prism$noHurtCam(CameraRenderState cameraState, PoseStack poseStack, CallbackInfo ci) {
        if (ModuleManager.NO_HURT_CAM.isEnabled()) {
            ci.cancel();
        }
    }
}
