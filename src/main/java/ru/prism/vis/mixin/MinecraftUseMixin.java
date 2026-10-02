package ru.prism.vis.mixin;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.prism.vis.PrismClient;

/** Счётчик ПКМ для CPS в HUD. */
@Mixin(Minecraft.class)
public abstract class MinecraftUseMixin {

    @Inject(method = "startUseItem", at = @At("HEAD"))
    private void prism$onUse(CallbackInfo ci) {
        PrismClient.recordUse();
    }
}
