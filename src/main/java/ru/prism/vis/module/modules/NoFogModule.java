package ru.prism.vis.module.modules;

import net.minecraft.client.Minecraft;
import ru.prism.vis.mixin.FogRendererAccessor;
import ru.prism.vis.module.Category;
import ru.prism.vis.module.Module;

/**
 * Полностью убирает туман (в том числе в Незере, под водой и от эффектов).
 * При выключении возвращает прежнее состояние.
 */
public class NoFogModule extends Module {
    private Boolean previous;

    public NoFogModule() {
        super("no_fog", "No Fog", "Убирает туман: Незер, вода, эффекты", Category.RENDER);
    }

    @Override
    public void onTick(Minecraft client) {
        if (isEnabled()) {
            boolean current = FogRendererAccessor.prism$isFogEnabled();
            if (previous == null) {
                previous = current;
            }
            if (current) {
                FogRendererAccessor.prism$setFogEnabled(false);
            }
        } else if (previous != null) {
            if (!FogRendererAccessor.prism$isFogEnabled() && previous) {
                FogRendererAccessor.prism$setFogEnabled(true);
            }
            previous = null;
        }
    }
}
