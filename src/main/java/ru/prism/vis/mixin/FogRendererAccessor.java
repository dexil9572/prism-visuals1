package ru.prism.vis.mixin;

import net.minecraft.client.renderer.fog.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Доступ к внутреннему флагу тумана (используется модулем No Fog). */
@Mixin(FogRenderer.class)
public interface FogRendererAccessor {

    @Accessor("fogEnabled")
    static boolean prism$isFogEnabled() {
        throw new AssertionError();
    }

    @Accessor("fogEnabled")
    static void prism$setFogEnabled(boolean value) {
        throw new AssertionError();
    }
}
