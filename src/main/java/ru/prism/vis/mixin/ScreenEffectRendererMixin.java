package ru.prism.vis.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.ModuleManager;

/** Low Fire: перед отрисовкой огня сдвигаем матрицу вниз. */
@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {

    @Inject(method = "submitFire", at = @At("HEAD"))
    private static void prism$lowFire(PoseStack poseStack, SubmitNodeCollector collector,
                                      TextureAtlasSprite sprite, CallbackInfo ci) {
        if (ModuleManager.LOW_FIRE.isEnabled()) {
            poseStack.translate(0.0, -(PrismConfig.get().lowFireOffset / 100.0), 0.0);
        }
    }
}
