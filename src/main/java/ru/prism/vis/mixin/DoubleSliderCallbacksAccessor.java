package ru.prism.vis.mixin;

import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Позволяет расширить максимум числового ползунка (гамма). */
@Mixin(SimpleOption.DoubleSliderCallbacks.class)
public interface DoubleSliderCallbacksAccessor {
    @Accessor("max")
    void prism$setMax(double max);
}
