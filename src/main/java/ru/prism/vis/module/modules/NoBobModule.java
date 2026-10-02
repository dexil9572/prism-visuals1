package ru.prism.vis.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import ru.prism.vis.module.Category;
import ru.prism.vis.module.Module;

/** Убирает покачивание камеры при ходьбе (bobView). */
public class NoBobModule extends Module {
    private Boolean previous;

    public NoBobModule() {
        super("no_bob", "No View Bobbing", "Отключает покачивание камеры при ходьбе", Category.RENDER);
    }

    @Override
    public void onTick(Minecraft client) {
        if (client.options == null) {
            return;
        }
        OptionInstance<Boolean> bob = client.options.bobView();
        if (isEnabled()) {
            if (previous == null) {
                previous = bob.get();
            }
            if (bob.get()) {
                bob.set(false);
            }
        } else if (previous != null) {
            bob.set(previous);
            previous = null;
        }
    }
}
