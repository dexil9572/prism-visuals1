package ru.prism.vis.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.ModuleManager;

/** Low Fire: перед отрисовкой огня сдвигаем матрицу вниз. */
@Mixin(InGameOverlayRenderer.class)
public abstract class InGameOverlayRendererMixin {

    @Inject(method = "renderFireOverlay", at = @At("HEAD"))
    private static void prism$lowFire(MinecraftClient client, MatrixStack matrices, CallbackInfo ci) {
        if (ModuleManager.LOW_FIRE.isEnabled()) {
            matrices.translate(0.0, -(PrismConfig.get().lowFireOffset / 100.0), 0.0);
        }
    }
}
