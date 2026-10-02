package ru.prism.vis.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.Category;
import ru.prism.vis.module.Module;
import ru.prism.vis.util.Colors;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Цветной взрыв частиц при ударе по сущности.
 * Вызывается из PrismClient по событию атаки (ClientPreAttackCallback).
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

    public static void spawn(Minecraft client, Entity target) {
        if (client.level == null || target == null) {
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
            DustParticleOptions dust = new DustParticleOptions(ARGB.opaque(rgb), 1.15f);

            double x = target.getX() + rnd(-0.45, 0.45);
            double y = target.getY() + target.getBbHeight() * 0.55 + rnd(-0.3, 0.3);
            double z = target.getZ() + rnd(-0.45, 0.45);

            client.level.addParticle(dust, x, y, z,
                    rnd(-0.28, 0.28), rnd(0.06, 0.4), rnd(-0.28, 0.28));
        }
    }

    private static double rnd(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }
}
