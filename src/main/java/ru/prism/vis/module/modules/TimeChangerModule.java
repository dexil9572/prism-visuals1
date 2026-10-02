package ru.prism.vis.module.modules;

import net.minecraft.client.Minecraft;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.Category;
import ru.prism.vis.module.Module;

/**
 * Клиентское время суток: видишь его только ты.
 * Серверное время не трогаем — подменяем значение часов на клиенте.
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

    /** Время суток (в тиках), которое сейчас должно быть на клиенте. */
    public static long targetTime() {
        int index = Math.floorMod(PrismConfig.get().timePreset, TIMES.length);
        return TIMES[index];
    }

    @Override
    public void onTick(Minecraft client) {
        if (!isEnabled() || client.level == null) {
            return;
        }
        client.level.setTimeFromServer(targetTime());
    }
}
