package ru.prism.vis;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.ModuleManager;
import ru.prism.vis.module.modules.CopyCoordsModule;
import ru.prism.vis.module.modules.HitParticlesModule;
import ru.prism.vis.module.modules.ToggleSprintModule;
import ru.prism.vis.render.BlockOverlayRenderer;
import ru.prism.vis.render.HudRenderer;
import ru.prism.vis.ui.PrismScreen;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Точка входа мода. Здесь регистрируются клавиши, модули,
 * HUD-элементы, рендер-события и загрузка/сохранение конфига.
 */
public class PrismClient implements ClientModInitializer {
    public static final String MOD_ID = "prism";
    public static final String VERSION = "1.1.0";

    private static KeyMapping zoomKey;
    private static KeyMapping sprintKey;
    private static KeyMapping guiKey;
    private static KeyMapping copyKey;

    // Очереди кликов для подсчёта CPS
    private static final Deque<Long> ATTACK_CLICKS = new ArrayDeque<>();
    private static final Deque<Long> USE_CLICKS = new ArrayDeque<>();

    @Override
    public void onInitializeClient() {
        PrismConfig.load();
        ModuleManager.init();

        KeyMapping.Category category =
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID, "main"));

        zoomKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.prism.zoom", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_C, category));
        sprintKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.prism.sprint", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, category));
        guiKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.prism.gui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));
        copyKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.prism.copypos", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ModuleManager.tick(client);
            while (sprintKey.consumeClick()) {
                ToggleSprintModule.onKey(client);
            }
            while (guiKey.consumeClick()) {
                if (client.gui.screen() == null) {
                    client.gui.setScreen(new PrismScreen());
                }
            }
            while (copyKey.consumeClick()) {
                if (client.gui.screen() == null && ModuleManager.COPY_POS.isEnabled()) {
                    CopyCoordsModule.copy(client);
                }
            }
        });

        // Удары по сущностям: частицы + CPS
        ClientPreAttackCallback.EVENT.register((client, player, clickCount) -> {
            if (clickCount != 0) {
                recordAttack();
                if (ModuleManager.HIT_PARTICLES.isEnabled() && client.hitResult instanceof EntityHitResult hit) {
                    HitParticlesModule.spawn(client, hit.getEntity());
                }
            }
            return false;
        });

        // HUD мода — после всех ванильных элементов
        HudElementRegistry.attachElementAfter(VanillaHudElements.SUBTITLES, id("hud"), HudRenderer::extract);

        // Свой прицел вместо ванильного
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (graphics, deltaTracker) -> {
            if (ModuleManager.CROSSHAIR.isEnabled()) {
                HudRenderer.extractCrosshair(graphics);
            } else {
                original.extractRenderState(graphics, deltaTracker);
            }
        });

        // Подсветка блока рисуется в мире
        LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register(BlockOverlayRenderer::render);

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> PrismConfig.save());
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static KeyMapping zoomKey() {
        return zoomKey;
    }

    // ============ CPS-счётчики (вызываются из миксинов/событий) ============

    public static void recordAttack() {
        push(ATTACK_CLICKS);
    }

    public static void recordUse() {
        push(USE_CLICKS);
    }

    public static int attackCps() {
        return prune(ATTACK_CLICKS);
    }

    public static int useCps() {
        return prune(USE_CLICKS);
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
