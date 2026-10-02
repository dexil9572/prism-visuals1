package ru.prism.vis.mixin;

import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.prism.vis.module.ModuleManager;
import ru.prism.vis.module.modules.TimeChangerModule;

/** Time Changer: подменяем клиентское время суток. */
@Mixin(Level.class)
public abstract class LevelClockMixin {

    @Inject(method = "getOverworldClockTime", at = @At("HEAD"), cancellable = true)
    private void prism$timeChangerOverworld(CallbackInfoReturnable<Long> cir) {
        prism$applyTimeChanger(cir);
    }

    @Inject(method = "getDefaultClockTime", at = @At("HEAD"), cancellable = true)
    private void prism$timeChangerDefault(CallbackInfoReturnable<Long> cir) {
        prism$applyTimeChanger(cir);
    }

    private static void prism$applyTimeChanger(CallbackInfoReturnable<Long> cir) {
        if (ModuleManager.TIME_CHANGER != null && ModuleManager.TIME_CHANGER.isEnabled()) {
            cir.setReturnValue(TimeChangerModule.targetTime());
        }
    }
}
