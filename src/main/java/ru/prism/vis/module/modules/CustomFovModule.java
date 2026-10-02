package ru.prism.vis.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import ru.prism.vis.PrismClient;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.Category;
import ru.prism.vis.module.Module;

/**
 * Свой угол обзора (FOV), не трогая ползунок в настройках игры.
 * Пока модуль включён — FOV держится на выбранном значении; при выключении
 * возвращается прежнее значение. Зум (C) имеет приоритет.
 */
public class CustomFovModule extends Module {
    private Integer previous;

    public CustomFovModule() {
        super("custom_fov", "Custom FOV", "Свой угол обзора, пока модуль включён", Category.RENDER);
        addRange("FOV", 30, 110, 5,
                () -> PrismConfig.get().customFov,
                v -> PrismConfig.get().customFov = v);
    }

    public static int target() {
        return Math.max(30, Math.min(110, PrismConfig.get().customFov));
    }

    @Override
    public void onTick(Minecraft client) {
        if (client.options == null) {
            return;
        }
        OptionInstance<Integer> fov = client.options.fov();
        if (isEnabled()) {
            if (previous == null) {
                previous = fov.get();
            }
            boolean zooming = PrismClient.zoomKey() != null && PrismClient.zoomKey().isDown();
            if (!zooming && fov.get() != target()) {
                fov.set(target());
            }
        } else if (previous != null) {
            fov.set(previous);
            previous = null;
        }
    }
}
