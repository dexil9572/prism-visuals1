package ru.prism.vis.module;

import net.minecraft.client.Minecraft;
import ru.prism.vis.config.PrismConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Базовый модуль: id, имя, описание, категория, настройки.
 * Состояние вкл/выкл хранится в конфиге.
 */
public class Module {
    public final String id;
    public final String name;
    public final String description;
    public final Category category;
    public final List<Setting> settings = new ArrayList<>();

    public Module(String id, String name, String description, Category category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public boolean isEnabled() {
        return PrismConfig.enabled(id);
    }

    public void toggle() {
        PrismConfig.setEnabled(id, !isEnabled());
    }

    /** Вызывается каждый клиентский тик, даже если модуль выключен. */
    public void onTick(Minecraft client) {
    }

    // ============ Фабрики настроек для GUI ============

    protected void addRange(String label, int min, int max, int step, IntSupplier get, IntConsumer set) {
        settings.add(Setting.range(label, min, max, step, get, set));
    }

    protected void addChoice(String label, String[] names, IntSupplier get, IntConsumer set) {
        settings.add(Setting.choice(label, names, get, set));
    }

    protected void addBool(String label, Supplier<Boolean> get, Consumer<Boolean> set) {
        settings.add(Setting.bool(label, get, set));
    }
}
