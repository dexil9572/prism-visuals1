package ru.prism.vis.module.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.Category;
import ru.prism.vis.module.Module;

/** Копирование координат в буфер обмена по клавише (по умолчанию V). */
public class CopyCoordsModule extends Module {
    private static final String[] FORMATS = {"X Y Z", "Команда /tp", "Чанк"};

    public CopyCoordsModule() {
        super("copy_pos", "Copy Coordinates", "Клавиша V копирует координаты в буфер", Category.PLAYER);
        addChoice("Формат", FORMATS,
                () -> PrismConfig.get().copyFormat,
                v -> PrismConfig.get().copyFormat = v);
    }

    /** Вызывается клиентом при нажатии клавиши копирования. */
    public static void copy(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }
        String text = switch (Math.floorMod(PrismConfig.get().copyFormat, FORMATS.length)) {
            case 1 -> String.format("/tp @s %.1f %.1f %.1f", player.getX(), player.getY(), player.getZ());
            case 2 -> (player.blockPosition().getX() >> 4) + " " + (player.blockPosition().getZ() >> 4);
            default -> String.format("%.1f %.1f %.1f", player.getX(), player.getY(), player.getZ());
        };
        client.keyboardHandler.setClipboard(text);
        if (client.gui != null) {
            client.gui.hud.setOverlayMessage(
                    Component.literal("Скопировано: " + text), false);
        }
    }
}
