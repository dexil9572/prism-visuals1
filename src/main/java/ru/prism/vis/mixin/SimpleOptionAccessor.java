package ru.prism.vis.mixin;

import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Доступ к callbacks опции — нужен для Full Bright. */
@Mixin(SimpleOption.class)
public interface SimpleOptionAccessor {
    @Accessor("callbacks")
    SimpleOption.ValueSetCallbacks<?> prism$callbacks();
}
