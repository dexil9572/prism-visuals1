package ru.prism.vis.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.prism.vis.module.ModuleManager;
import ru.prism.vis.render.HudRenderer;

/**
 * 1) В конце рендера HUD рисуем весь HUD мода.
 * 2) Если включён кастомный прицел — отменяем ванильный.
 */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void prism$renderHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        HudRenderer.render(context, tickCounter);
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void prism$hideVanillaCrosshair(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (ModuleManager.CROSSHAIR.isEnabled()
                && !MinecraftClient.getInstance().options.debugEnabled) {
            ci.cancel();
        }
    }
}
