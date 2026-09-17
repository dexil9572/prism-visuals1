package ru.prism.vis.module.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.particle.DustParticleEffect;
import org.joml.Vector3f;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.Category;
import ru.prism.vis.module.Module;
import ru.prism.vis.util.Colors;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Цветной взрыв частиц при ударе по сущности.
 * Вызывается из MinecraftClientMixin (doAttack).
 */
public class HitParticlesModule extends Module {
    public HitParticlesModule() {
        super("hit_particles", "Hit Particles", "Цветной всплеск частиц при ударе", Category.RENDER);
        addRange("Количество", 4, 60, 2,
                () -> PrismConfig.get().particleCount,
                v -> PrismConfig.get().particleCount = v);
        addChoice("Цвет", Colors.NAMES,
                () -> PrismConfig.get().particleColor,
                v -> PrismConfig.get().particleColor = v);
        addBool("Радуга",
                () -> PrismConfig.get().particleRainbow,
                v -> PrismConfig.get().particleRainbow = v);
    }

    public static void spawn(MinecraftClient client, Entity target) {
        if (client.particleManager == null) {
            return;
        }
        PrismConfig.Data cfg = PrismConfig.get();
        float baseHue = (float) ((System.currentTimeMillis() % 3000L) / 3000.0);

        for (int i = 0; i < cfg.particleCount; i++) {
            int rgb;
            if (cfg.particleRainbow) {
                // смещаем оттенок — веер цветов в одном ударе
                rgb = Colors.hsb(baseHue + i * 0.045f, 0.85f, 1f);
            } else {
                rgb = Colors.palette(cfg.particleColor);
            }
            float[] c = Colors.rgbf(rgb);
            DustParticleEffect dust = new DustParticleEffect(new Vector3f(c[0], c[1], c[2]), 1.15f);

            double x = target.getX() + rnd(-0.45, 0.45);
            double y = target.getY() + target.getHeight() * 0.55 + rnd(-0.3, 0.3);
            double z = target.getZ() + rnd(-0.45, 0.45);

            client.particleManager.addParticle(dust, x, y, z,
                    rnd(-0.28, 0.28), rnd(0.06, 0.4), rnd(-0.28, 0.28));
        }
    }

    private static double rnd(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
}
