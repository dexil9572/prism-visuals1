package ru.prism.vis.module;

import net.minecraft.client.Minecraft;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.modules.ClearVisionModule;
import ru.prism.vis.module.modules.CopyCoordsModule;
import ru.prism.vis.module.modules.CustomFovModule;
import ru.prism.vis.module.modules.FullBrightModule;
import ru.prism.vis.module.modules.HitParticlesModule;
import ru.prism.vis.module.modules.NoBobModule;
import ru.prism.vis.module.modules.NoFogModule;
import ru.prism.vis.module.modules.ParticlesModule;
import ru.prism.vis.module.modules.TimeChangerModule;
import ru.prism.vis.module.modules.ToggleSprintModule;
import ru.prism.vis.module.modules.ZoomModule;
import ru.prism.vis.util.Colors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Реестр всех модулей. Модули без логики создаются прямо здесь. */
public final class ModuleManager {
    private static final List<Module> MODULES = new ArrayList<>();

    public static HitParticlesModule HIT_PARTICLES;
    public static FullBrightModule FULL_BRIGHT;
    public static ZoomModule ZOOM;
    public static ToggleSprintModule TOGGLE_SPRINT;
    public static TimeChangerModule TIME_CHANGER;
    public static NoBobModule NO_BOB;
    public static CustomFovModule CUSTOM_FOV;
    public static ClearVisionModule CLEAR_VISION;
    public static ParticlesModule PARTICLES;
    public static NoFogModule NO_FOG;
    public static CopyCoordsModule COPY_POS;

    public static Module NO_HURT_CAM;
    public static Module LOW_FIRE;
    public static Module KEYSTROKES;
    public static Module HUD_INFO;
    public static Module ARMOR_HUD;
    public static Module CROSSHAIR;
    public static Module BLOCK_OVERLAY;
    public static Module HIDE_HAND;
    public static Module ITEM_INFO;

    public static void init() {
        HIT_PARTICLES = add(new HitParticlesModule());
        FULL_BRIGHT = add(new FullBrightModule());
        ZOOM = add(new ZoomModule());
        TOGGLE_SPRINT = add(new ToggleSprintModule());
        TIME_CHANGER = add(new TimeChangerModule());
        NO_BOB = add(new NoBobModule());
        CUSTOM_FOV = add(new CustomFovModule());
        CLEAR_VISION = add(new ClearVisionModule());
        PARTICLES = add(new ParticlesModule());
        NO_FOG = add(new NoFogModule());
        COPY_POS = add(new CopyCoordsModule());

        NO_HURT_CAM = add(new Module(
                "no_hurt_cam", "No Hurt Cam",
                "Убирает тряску камеры при получении урона", Category.RENDER));

        LOW_FIRE = add(new Module(
                "low_fire", "Low Fire",
                "Опускает текстуру огня, не закрывает обзор", Category.RENDER) {{
            addRange("Высота %", 10, 50, 5,
                    () -> PrismConfig.get().lowFireOffset,
                    v -> PrismConfig.get().lowFireOffset = v);
        }});

        KEYSTROKES = add(new Module(
                "keystrokes", "Keystrokes",
                "Оверлей WASD / SPACE / ЛКМ / ПКМ", Category.HUD) {{
            addBool("Радуга",
                    () -> PrismConfig.get().keysRainbow,
                    v -> PrismConfig.get().keysRainbow = v);
            addBool("Показывать CPS",
                    () -> PrismConfig.get().keysCps,
                    v -> PrismConfig.get().keysCps = v);
        }});

        HUD_INFO = add(new Module(
                "hud_info", "HUD Info",
                "FPS, координаты, биом, CPS, время", Category.HUD) {{
            addBool("Радуга",
                    () -> PrismConfig.get().hudRainbow,
                    v -> PrismConfig.get().hudRainbow = v);
        }});

        ARMOR_HUD = add(new Module(
                "armor_hud", "Armor HUD",
                "Броня и прочность над хотбаром", Category.HUD) {{
            addBool("Проценты прочности",
                    () -> PrismConfig.get().armorPercent,
                    v -> PrismConfig.get().armorPercent = v);
        }});

        CROSSHAIR = add(new Module(
                "crosshair", "Crosshair",
                "Прицел с динамическим разбросом и пульсом", Category.HUD) {{
            addRange("Размер", 2, 10, 1,
                    () -> PrismConfig.get().crossSize,
                    v -> PrismConfig.get().crossSize = v);
            addRange("Разброс", 0, 8, 1,
                    () -> PrismConfig.get().crossGap,
                    v -> PrismConfig.get().crossGap = v);
            addChoice("Цвет", Colors.NAMES,
                    () -> PrismConfig.get().crossColor,
                    v -> PrismConfig.get().crossColor = v);
            addBool("Радуга",
                    () -> PrismConfig.get().crossRainbow,
                    v -> PrismConfig.get().crossRainbow = v);
        }});

        HIDE_HAND = add(new Module(
                "hide_hand", "Hide Hand",
                "Скрывает руки и предмет в первом лице", Category.RENDER));

        ITEM_INFO = add(new Module(
                "item_info", "Item Info",
                "Название, прочность и количество предмета в руке", Category.HUD) {{
            addBool("Название",
                    () -> PrismConfig.get().itemInfoName,
                    v -> PrismConfig.get().itemInfoName = v);
            addBool("Прочность",
                    () -> PrismConfig.get().itemInfoDurability,
                    v -> PrismConfig.get().itemInfoDurability = v);
            addBool("Количество",
                    () -> PrismConfig.get().itemInfoCount,
                    v -> PrismConfig.get().itemInfoCount = v);
        }});

        BLOCK_OVERLAY = add(new Module(
                "block_overlay", "Block Overlay",
                "Цветная обводка и заливка блока под прицелом", Category.WORLD) {{
            addChoice("Цвет", Colors.NAMES,
                    () -> PrismConfig.get().overlayColor,
                    v -> PrismConfig.get().overlayColor = v);
            addRange("Заливка %", 0, 100, 5,
                    () -> PrismConfig.get().overlayAlpha,
                    v -> PrismConfig.get().overlayAlpha = v);
            addBool("Заливка",
                    () -> PrismConfig.get().overlayFill,
                    v -> PrismConfig.get().overlayFill = v);
        }});
    }

    private static <T extends Module> T add(T module) {
        MODULES.add(module);
        return module;
    }

    public static List<Module> modules() {
        return Collections.unmodifiableList(MODULES);
    }

    public static void tick(Minecraft client) {
        for (Module module : MODULES) {
            module.onTick(client);
        }
    }

    private ModuleManager() {
    }
}
