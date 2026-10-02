package ru.prism.vis.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import ru.prism.vis.mixin.OptionInstanceValueAccessor;
import ru.prism.vis.module.Category;
import ru.prism.vis.module.Module;

/**
 * Full Bright: выкручиваем гамму далеко за пределы ползунка
 * (значение пишется прямо в OptionInstance, минуя валидацию 0..1).
 */
public class FullBrightModule extends Module {
    /** Значение гаммы, при котором ночь становится как день. */
    private static final double BRIGHT = 10.0;

    private boolean applied;
    private double previous = 0.5;

    public FullBrightModule() {
        super("full_bright", "Full Bright", "Максимальная яркость — ночь как день", Category.RENDER);
    }

    @Override
    public void onTick(Minecraft client) {
        if (client.options == null) {
            return;
        }
        OptionInstance<Double> gamma = client.options.gamma();

        if (isEnabled()) {
            if (!applied) {
                previous = gamma.get();
                applied = true;
            }
            if (gamma.get() < BRIGHT) {
                ((OptionInstanceValueAccessor) (Object) gamma).prism$setValue(BRIGHT);
            }
        } else if (applied) {
            ((OptionInstanceValueAccessor) (Object) gamma).prism$setValue(previous);
            applied = false;
        }
    }
}
