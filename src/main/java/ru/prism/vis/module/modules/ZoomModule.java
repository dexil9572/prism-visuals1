package ru.prism.vis.module.modules;

import net.minecraft.client.Minecraft;
import ru.prism.vis.PrismClient;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.Category;
import ru.prism.vis.module.Module;

/**
 * Плавный зум: сдвигаем FOV к цели с затуханием, пока зажата клавиша C.
 */
public class ZoomModule extends Module {
    private Integer baseFov;
    private float fade;

    public ZoomModule() {
        super("zoom", "Zoom", "Плавный зум, пока зажата клавиша C", Category.PLAYER);
        addRange("Сила %", 10, 90, 5,
                () -> PrismConfig.get().zoomStrength,
                v -> PrismConfig.get().zoomStrength = v);
        addBool("Плавность",
                () -> PrismConfig.get().zoomSmoothCam,
                v -> PrismConfig.get().zoomSmoothCam = v);
    }

    @Override
    public void onTick(Minecraft client) {
        if (client.options == null) {
            return;
        }
        boolean want = isEnabled()
                && PrismClient.zoomKey() != null
                && PrismClient.zoomKey().isDown()
                && client.gui.screen() == null;

        if (want && baseFov == null) {
            baseFov = client.options.fov().get();
        }

        float speed = PrismConfig.get().zoomSmoothCam ? 0.35f : 1.0f;
        fade += ((want ? 1f : 0f) - fade) * speed;

        if (baseFov != null) {
            double strength = PrismConfig.get().zoomStrength / 100.0;
            int fov = (int) Math.round(baseFov * (1.0 - strength * fade));
            client.options.fov().set(Math.max(30, Math.min(110, fov)));

            if (!want && fade < 0.03f) {
                client.options.fov().set(baseFov);
                baseFov = null;
                fade = 0f;
            }
        }
    }
}
