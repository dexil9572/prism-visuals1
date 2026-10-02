package ru.prism.vis.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.Category;
import ru.prism.vis.module.Module;

/**
 * «Чистое зрение»: убирает накладываемые на экран эффекты.
 *  - оверлеи: огонь, вода, портал, тыква, тошнота (screenEffectScale)
 *  - затемнение: Warden / слепота (darknessEffectScale)
 *  - эффект FOV: расширение обзора от спринта/зелий (fovEffectScale)
 */
public class ClearVisionModule extends Module {
    private Double prevScreen;
    private Double prevDarkness;
    private Double prevFovEffect;

    public ClearVisionModule() {
        super("clear_vision", "Clear Vision", "Убирает оверлеи, затемнение и эффект FOV", Category.RENDER);
        addBool("Оверлеи (огонь, вода, портал)",
                () -> PrismConfig.get().clearOverlays,
                v -> PrismConfig.get().clearOverlays = v);
        addBool("Затемнение (Warden, слепота)",
                () -> PrismConfig.get().clearDarkness,
                v -> PrismConfig.get().clearDarkness = v);
        addBool("Эффект FOV (спринт, зелья)",
                () -> PrismConfig.get().clearFovEffects,
                v -> PrismConfig.get().clearFovEffects = v);
    }

    @Override
    public void onTick(Minecraft client) {
        if (client.options == null) {
            return;
        }
        OptionInstance<Double> screen = client.options.screenEffectScale();
        OptionInstance<Double> darkness = client.options.darknessEffectScale();
        OptionInstance<Double> fovEffect = client.options.fovEffectScale();

        if (isEnabled()) {
            if (prevScreen == null) {
                prevScreen = screen.get();
                prevDarkness = darkness.get();
                prevFovEffect = fovEffect.get();
            }
            PrismConfig.Data cfg = PrismConfig.get();
            screen.set(cfg.clearOverlays ? 0.0 : prevScreen);
            darkness.set(cfg.clearDarkness ? 0.0 : prevDarkness);
            fovEffect.set(cfg.clearFovEffects ? 0.0 : prevFovEffect);
        } else if (prevScreen != null) {
            screen.set(prevScreen);
            darkness.set(prevDarkness);
            fovEffect.set(prevFovEffect);
            prevScreen = null;
            prevDarkness = null;
            prevFovEffect = null;
        }
    }
}
