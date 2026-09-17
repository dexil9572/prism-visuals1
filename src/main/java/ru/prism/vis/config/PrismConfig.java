package ru.prism.vis.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Конфиг мода: .minecraft/config/prism.json
 * Хранит включённые модули и все настройки.
 */
public class PrismConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Data data = new Data();

    public static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("prism.json");
    }

    public static Data get() {
        return data;
    }

    public static boolean enabled(String id) {
        return data.enabled.getOrDefault(id, false);
    }

    public static void setEnabled(String id, boolean value) {
        data.enabled.put(id, value);
        save();
    }

    public static void load() {
        try {
            if (Files.exists(file())) {
                try (Reader reader = Files.newBufferedReader(file())) {
                    Data loaded = GSON.fromJson(reader, Data.class);
                    if (loaded != null) {
                        data = loaded;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        data.fillDefaults();
    }

    public static void save() {
        try {
            Files.createDirectories(file().getParent());
            try (Writer writer = Files.newBufferedWriter(file())) {
                GSON.toJson(data, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Все значения по умолчанию — тут. */
    public static class Data {
        public Map<String, Boolean> enabled = new HashMap<>();

        // Hit Particles
        public int particleCount = 18;
        public int particleColor = 0;
        public boolean particleRainbow = true;

        // Low Fire (в процентах высоты экрана)
        public int lowFireOffset = 35;

        // Zoom (процент приближения)
        public int zoomStrength = 70;
        public boolean zoomSmoothCam = true;

        // Toggle Sprint
        public boolean sprintToggled = false;

        // Keystrokes
        public boolean keysRainbow = false;

        // HUD Info
        public boolean hudRainbow = true;

        // Crosshair
        public int crossSize = 4;
        public int crossGap = 2;
        public int crossColor = 0;
        public boolean crossRainbow = true;

        // Block Overlay
        public int overlayColor = 0;
        public int overlayAlpha = 30;
        public boolean overlayFill = true;

        // Time Changer
        public int timePreset = 0;

        void fillDefaults() {
            if (enabled == null) {
                enabled = new HashMap<>();
            }
            Map<String, Boolean> defaults = new HashMap<>();
            defaults.put("hit_particles", true);
            defaults.put("no_hurt_cam", true);
            defaults.put("low_fire", true);
            defaults.put("zoom", true);
            defaults.put("hud_info", true);
            defaults.put("crosshair", true);
            defaults.put("block_overlay", true);
            defaults.put("full_bright", false);
            defaults.put("toggle_sprint", false);
            defaults.put("keystrokes", false);
            defaults.put("armor_hud", false);
            defaults.put("time_changer", false);
            defaults.forEach(enabled::putIfAbsent);
        }
    }
}
