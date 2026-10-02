package ru.prism.vis.mixin;

import net.minecraft.client.OptionInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Прямой доступ к значению опции — нужен для Full Bright (гамма > 1.0). */
@Mixin(OptionInstance.class)
public interface OptionInstanceValueAccessor {
    @Accessor("value")
    void prism$setValue(Object value);
}
