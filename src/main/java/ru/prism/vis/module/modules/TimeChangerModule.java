package ru.prism.vis.module.modules;

import net.minecraft.client.MinecraftClient;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.Category;
import ru.prism.vis.module.Module;

/**
 * Клиентское время суток: видишь его только ты.
 * Серверное время не трогаем — меняем значение на клиенте каждый тик.
 */
public class TimeChangerModule extends Module {
    private static final long[] TIMES = {1000L, 6000L, 12500L, 18000L};
    private static final String[] NAMES = {"Утро", "Полдень", "Закат", "Полночь"};

    public TimeChangerModule() {
        super("time_changer", "Time Changer", "Фиксированное клиентское время суток", Category.WORLD);
        addChoice("Время", NAMES,
                () -> PrismConfig.get().timePreset,
                v -> PrismConfig.get().timePreset = v);
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (!isEnabled() || client.world == null) {
            return;
        }
        int index = Math.floorMod(PrismConfig.get().timePreset, TIMES.length);
        client.world.getLevelProperties().setTimeOfDay(TIMES[index]);
    }
}
