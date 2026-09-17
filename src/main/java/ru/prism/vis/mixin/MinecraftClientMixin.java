package ru.prism.vis.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.prism.vis.PrismClient;
import ru.prism.vis.module.ModuleManager;
import ru.prism.vis.module.modules.HitParticlesModule;

/**
 * Перехват ударов и ПКМ:
 * - CPS-счётчики
 * - частицы при ударе по сущности
 */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    @Nullable
    public HitResult crosshairTarget;

    @Inject(method = "doAttack", at = @At("HEAD"))
    private void prism$onAttack(CallbackInfoReturnable<Boolean> cir) {
        PrismClient.recordAttack();
        if (ModuleManager.HIT_PARTICLES.isEnabled()
                && this.crosshairTarget != null
                && this.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            EntityHitResult hit = (EntityHitResult) this.crosshairTarget;
            HitParticlesModule.spawn(MinecraftClient.getInstance(), hit.getEntity());
        }
    }

    @Inject(method = "doItemUse", at = @At("HEAD"))
    private void prism$onUse(CallbackInfo ci) {
        PrismClient.recordUse();
    }
}
