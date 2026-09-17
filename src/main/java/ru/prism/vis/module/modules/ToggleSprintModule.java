package ru.prism.vis.module.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.Category;
import ru.prism.vis.module.Module;

/** Автоспринт по клавише G. Состояние пишется в конфиг. */
public class ToggleSprintModule extends Module {
    public ToggleSprintModule() {
        super("toggle_sprint", "Toggle Sprint", "Автоспринт по клавише G", Category.PLAYER);
    }

    public static void onKey(MinecraftClient client) {
        boolean value = !PrismConfig.get().sprintToggled;
        PrismConfig.get().sprintToggled = value;
        PrismConfig.save();
        if (client.player != null) {
            client.player.sendMessage(
                    Text.literal("Prism · Автоспринт: " + (value ? "ВКЛ" : "ВЫКЛ")), true);
        }
    }

    @Override
    public void onTick(MinecraftClient client) {
        if (!isEnabled() || !PrismConfig.get().sprintToggled) {
            return;
        }
        ClientPlayerEntity player = client.player;
        if (player != null && player.input.movementForward > 0 && !player.isSprinting()) {
            player.setSprinting(true);
        }
    }
}
