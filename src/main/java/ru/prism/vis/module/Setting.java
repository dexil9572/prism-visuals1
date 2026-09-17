package ru.prism.vis.module;

import ru.prism.vis.config.PrismConfig;

import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Настройка модуля, которую видно в Prism GUI:
 * [-] значение [+]. Значения зацикливаются.
 */
public abstract class Setting {
    public final String label;

    protected Setting(String label) {
        this.label = label;
    }

    public abstract String display();

    protected abstract void change(int dir);

    public void click(boolean decrease) {
        change(decrease ? -1 : 1);
        PrismConfig.save();
    }

    public static Setting range(String label, int min, int max, int step, IntSupplier get, IntConsumer set) {
        return new Setting(label) {
            @Override
            public String display() {
                return String.valueOf(get.getAsInt());
            }

            @Override
            protected void change(int dir) {
                int v = get.getAsInt() + dir * step;
                if (v > max) v = min;
                if (v < min) v = max;
                set.accept(v);
            }
        };
    }

    public static Setting choice(String label, String[] names, IntSupplier get, IntConsumer set) {
        return new Setting(label) {
            @Override
            public String display() {
                return names[Math.floorMod(get.getAsInt(), names.length)];
            }

            @Override
            protected void change(int dir) {
                set.accept(Math.floorMod(get.getAsInt() + dir, names.length));
            }
        };
    }

    public static Setting bool(String label, Supplier<Boolean> get, Consumer<Boolean> set) {
        return new Setting(label) {
            @Override
            public String display() {
                return get.get() ? "Вкл" : "Выкл";
            }

            @Override
            protected void change(int dir) {
                set.accept(!get.get());
            }
        };
    }
}
