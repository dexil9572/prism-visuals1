package ru.prism.vis.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.prism.vis.module.ModuleManager;

/** Hide Hand: не рисуем руки/предмет в первом лице. */
@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Inject(method = "submitHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void prism$hideHand(float partialTick, PoseStack poseStack, SubmitNodeCollector collector,
                                LocalPlayer player, int light, CallbackInfo ci) {
        if (ModuleManager.HIDE_HAND != null && ModuleManager.HIDE_HAND.isEnabled()) {
            ci.cancel();
        }
    }
}
