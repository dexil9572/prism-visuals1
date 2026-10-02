package ru.prism.vis.mixin;

import net.minecraft.client.ClientClockManager;
import net.minecraft.core.Holder;
import net.minecraft.world.clock.WorldClock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.prism.vis.module.ModuleManager;
import ru.prism.vis.module.modules.TimeChangerModule;

/** Time Changer: клиентские часы всегда показывают выбранное время. */
@Mixin(ClientClockManager.class)
public abstract class ClientClockManagerMixin {

    @Inject(method = "getTotalTicks", at = @At("HEAD"), cancellable = true)
    private void prism$timeChanger(Holder<WorldClock> clock, CallbackInfoReturnable<Long> cir) {
        if (ModuleManager.TIME_CHANGER != null && ModuleManager.TIME_CHANGER.isEnabled()) {
            cir.setReturnValue(TimeChangerModule.targetTime());
        }
    }
}
