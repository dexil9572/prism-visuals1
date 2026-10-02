package ru.prism.vis.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.server.level.ParticleStatus;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.Category;
import ru.prism.vis.module.Module;

/** Управление количеством частиц (полезно для FPS). */
public class ParticlesModule extends Module {
    private static final String[] NAMES = {"Все", "Меньше", "Минимум"};
    private static final ParticleStatus[] STATUS =
            {ParticleStatus.ALL, ParticleStatus.DECREASED, ParticleStatus.MINIMAL};

    private ParticleStatus previous;

    public ParticlesModule() {
        super("particles", "Particles", "Сколько частиц рисует игра", Category.RENDER);
        addChoice("Частицы", NAMES,
                () -> PrismConfig.get().particleMode,
                v -> PrismConfig.get().particleMode = v);
    }

    public static ParticleStatus target() {
        return STATUS[Math.floorMod(PrismConfig.get().particleMode, STATUS.length)];
    }

    @Override
    public void onTick(Minecraft client) {
        if (client.options == null) {
            return;
        }
        OptionInstance<ParticleStatus> particles = client.options.particles();
        if (isEnabled()) {
            if (previous == null) {
                previous = particles.get();
            }
            if (particles.get() != target()) {
                particles.set(target());
            }
        } else if (previous != null) {
            particles.set(previous);
            previous = null;
        }
    }
}
