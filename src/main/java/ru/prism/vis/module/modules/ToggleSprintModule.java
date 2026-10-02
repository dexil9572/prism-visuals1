package ru.prism.vis.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.Category;
import ru.prism.vis.module.Module;

/** Автоспринт по клавише G. Состояние пишется в конфиг. */
public class ToggleSprintModule extends Module {
    public ToggleSprintModule() {
        super("toggle_sprint", "Toggle Sprint", "Автоспринт по клавише G", Category.PLAYER);
    }

    public static void onKey(Minecraft client) {
        boolean value = !PrismConfig.get().sprintToggled;
        PrismConfig.get().sprintToggled = value;
        PrismConfig.save();
        if (client.player != null) {
            client.player.sendOverlayMessage(
                    Component.literal("Prism · Автоспринт: " + (value ? "ВКЛ" : "ВЫКЛ")));
        }
    }

    @Override
    public void onTick(Minecraft client) {
        if (!isEnabled() || !PrismConfig.get().sprintToggled) {
            return;
        }
        LocalPlayer player = client.player;
        if (player != null && player.input != null && player.input.hasForwardImpulse() && !player.isSprinting()) {
            player.setSprinting(true);
        }
    }
}
