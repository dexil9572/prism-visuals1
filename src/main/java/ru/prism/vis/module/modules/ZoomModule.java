package ru.prism.vis.module.modules;

import net.minecraft.client.MinecraftClient;
import ru.prism.vis.PrismClient;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.Category;
import ru.prism.vis.module.Module;

/**
 * Плавный зум: сдвигаем FOV к цели с затуханием,
 * опционально включаем кинематографичную камеру.
 */
public class ZoomModule extends Module {
    private Integer baseFov;
    private boolean baseSmooth;
    private float fade;

    public ZoomModule() {
        super("zoom", "Zoom", "Плавный зум, пока зажата клавиша C", Category.PLAYER);
        addRange("Сила %", 10, 90, 5,
                () -> PrismConfig.get().zoomStrength,
                v -> PrismConfig.get().zoomStrength = v);
        addBool("Кино-камера",
                () -> PrismConfig.get().zoomSmoothCam,
                v -> PrismConfig.get().zoomSmoothCam = v);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (client.options == null) {
            return;
        }
        boolean want = isEnabled()
                && PrismClient.zoomKey() != null
                && PrismClient.zoomKey().isPressed()
                && client.currentScreen == null;

        if (want && baseFov == null) {
            baseFov = client.options.getFov().getValue();
            baseSmooth = client.options.smoothCameraEnabled;
        }

        fade += ((want ? 1f : 0f) - fade) * 0.35f;

        if (baseFov != null) {
            double strength = PrismConfig.get().zoomStrength / 100.0;
            int fov = (int) Math.round(baseFov * (1.0 - strength * fade));
            client.options.getFov().setValue(Math.max(30, Math.min(110, fov)));

            if (PrismConfig.get().zoomSmoothCam) {
                client.options.smoothCameraEnabled = fade > 0.05f;
            }

            if (!want && fade < 0.03f) {
                client.options.getFov().setValue(baseFov);
                client.options.smoothCameraEnabled = baseSmooth;
                baseFov = null;
                fade = 0f;
            }
        }
    }
}
