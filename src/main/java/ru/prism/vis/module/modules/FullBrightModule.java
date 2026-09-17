package ru.prism.vis.module.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import ru.prism.vis.mixin.DoubleSliderCallbacksAccessor;
import ru.prism.vis.mixin.SimpleOptionAccessor;
import ru.prism.vis.module.Category;
import ru.prism.vis.module.Module;

/**
 * Full Bright: расширяем лимит ползунка яркости через accessor-миксины
 * и выкручиваем гамму до x1500.
 */
public class FullBrightModule extends Module {
    private boolean applied;

    public FullBrightModule() {
        super("full_bright", "Full Bright", "Максимальная яркость — ночь как день", Category.RENDER);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.options == null) {
            return;
        }
        if (isEnabled() && !applied) {
            apply(client, true);
            applied = true;
        } else if (!isEnabled() && applied) {
            apply(client, false);
            applied = false;
        } else if (isEnabled() && client.options.getGamma().getValue() < 500.0) {
            // игрок мог сбросить яркость в настройках видео — возвращаем
            client.options.getGamma().setValue(1500.0);
        }
    }

    private void apply(MinecraftClient client, boolean on) {
        try {
            SimpleOption<Double> gamma = client.options.getGamma();
            Object callbacks = ((SimpleOptionAccessor) (Object) gamma).prism$callbacks();
            if (callbacks instanceof SimpleOption.DoubleSliderCallbacks) {
                DoubleSliderCallbacksAccessor accessor = (DoubleSliderCallbacksAccessor) callbacks;
                accessor.prism$setMax(on ? 1500.0 : 1.0);
            }
            gamma.setValue(on ? 1500.0 : 1.0);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
