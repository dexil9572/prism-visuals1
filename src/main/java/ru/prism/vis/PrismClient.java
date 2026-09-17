package ru.prism.vis;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.ModuleManager;
import ru.prism.vis.module.modules.ToggleSprintModule;
import ru.prism.vis.render.OverlayRenderer;
import ru.prism.vis.ui.PrismScreen;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Точка входа мода. Здесь регистрируются клавиши, модули,
 * рендер- события и загрузка/сохранение конфига.
 */
public class PrismClient implements ClientModInitializer {
    public static final String MOD_ID = "prism";
    public static final String VERSION = "1.0.0";

    private static KeyBinding zoomKey;
    private static KeyBinding sprintKey;
    private static KeyBinding guiKey;

    // Очереди кликов для подсчёта CPS
    private static final Deque<Long> attackClicks = new ArrayDeque<>();
    private static final Deque<Long> useClicks = new ArrayDeque<>();

    @Override
    public void onInitializeClient() {
        PrismConfig.load();
        ModuleManager.init();

        zoomKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.prism.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "key.categories.prism"));
        sprintKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.prism.sprint", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.prism"));
        guiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.prism.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, "key.categories.prism"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ModuleManager.tick(client);
            while (sprintKey.wasPressed()) {
                ToggleSprintModule.onKey(client);
            }
            while (guiKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new PrismScreen());
                }
            }
        });

        // Подсветка блока рисуется в мире
        WorldRenderEvents.LAST.register(OverlayRenderer::renderLast);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> PrismConfig.save());
    }

    public static KeyBinding zoomKey() {
        return zoomKey;
    }

    // ============ CPS-счётчики (вызываются из миксина) ============

    public static void recordAttack() {
        push(attackClicks);
    }

    public static void recordUse() {
        push(useClicks);
    }

    public static int attackCps() {
        return prune(attackClicks);
    }

    public static int useCps() {
        return prune(useClicks);
    }

    private static void push(Deque<Long> queue) {
        queue.addLast(System.currentTimeMillis());
    }

    private static int prune(Deque<Long> queue) {
        long now = System.currentTimeMillis();
        while (!queue.isEmpty() && now - queue.peekFirst() > 1000L) {
            queue.pollFirst();
        }
        return queue.size();
    }
}
