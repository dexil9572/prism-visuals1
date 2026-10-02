package ru.prism.vis.render;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import ru.prism.vis.PrismClient;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.ModuleManager;
import ru.prism.vis.util.Colors;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Весь HUD мода рисуется здесь.
 * Регистрируется как HUD-элемент Fabric API и вызывается в конце отрисовки HUD.
 */
public final class HudRenderer {
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");

    private HudRenderer() {
    }

    public static void extract(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gui.hud.isHidden()) {
            return;
        }

        if (ModuleManager.KEYSTROKES.isEnabled()) {
            renderKeystrokes(graphics, client);
        }
        if (ModuleManager.HUD_INFO.isEnabled()) {
            renderInfo(graphics, client);
        }
        if (ModuleManager.ARMOR_HUD.isEnabled()) {
            renderArmor(graphics, client);
        }
        if (ModuleManager.ITEM_INFO.isEnabled()) {
            renderItemInfo(graphics, client);
        }
        if (ModuleManager.TOGGLE_SPRINT.isEnabled() && PrismConfig.get().sprintToggled) {
            graphics.centeredText(client.font, "Спринт [ВКЛ]",
                    graphics.guiWidth() / 2, graphics.guiHeight() - 76, accent());
        }
    }

    private static int accent() {
        return PrismConfig.get().hudRainbow ? Colors.rainbow() : Colors.argb(Colors.palette(0), 1f);
    }

    // ============ Кастомный прицел ============

    public static void extractCrosshair(GuiGraphicsExtractor graphics) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }
        PrismConfig.Data cfg = PrismConfig.get();

        int cx = graphics.guiWidth() / 2;
        int cy = graphics.guiHeight() / 2;

        double speed = Math.hypot(player.getDeltaMovement().x, player.getDeltaMovement().z);
        float cooldown = player.getAttackStrengthScale(0.0f);

        int gap = cfg.crossGap + (int) Math.round(speed * 10.0) + (int) ((1f - cooldown) * 3f);
        int size = cfg.crossSize;
        int color = cfg.crossRainbow ? Colors.rainbow() : Colors.argb(Colors.palette(cfg.crossColor), 1f);

        // центральная точка
        graphics.fill(cx - 1, cy - 1, cx + 1, cy + 1, color);
        // лучи
        graphics.fill(cx - gap - size, cy, cx - gap, cy + 1, color);
        graphics.fill(cx + gap, cy, cx + gap + size, cy + 1, color);
        graphics.fill(cx, cy - gap - size, cx + 1, cy - gap, color);
        graphics.fill(cx, cy + gap, cx + 1, cy + gap + size, color);

        // полоса перезарядки атаки
        if (cooldown < 1f) {
            int w = size * 4;
            int y = cy + gap + size + 3;
            graphics.fill(cx - w / 2, y, cx + w / 2, y + 2, 0x66000000);
            graphics.fill(cx - w / 2, y, cx - w / 2 + (int) (w * cooldown), y + 2, color);
        }
    }

    // ============ Информационная панель ============

    private static void renderInfo(GuiGraphicsExtractor graphics, Minecraft client) {
        Font font = client.font;
        LocalPlayer player = client.player;
        String biome = "?";
        if (client.level != null) {
            biome = client.level.getBiome(player.blockPosition()).unwrapKey()
                    .map(key -> key.identifier().getPath()).orElse("?");
        }

        String[] lines = {
                "PRISM " + PrismClient.VERSION,
                client.getFps() + " FPS  ·  " + ping(client),
                String.format("XYZ  %.1f  %.1f  %.1f", player.getX(), player.getY(), player.getZ()),
                "F " + player.getDirection().getName() + "  ·  " + biome,
                "CPS  " + PrismClient.attackCps() + " | " + PrismClient.useCps(),
                LocalTime.now().format(CLOCK)
        };

        int w = 0;
        for (String line : lines) {
            w = Math.max(w, font.width(line));
        }

        int x = 6;
        int y = 6;
        int h = lines.length * 11 + 6;

        graphics.fill(x - 3, y - 3, x + w + 6, y + h - 3, 0x66070A12);
        graphics.fill(x - 3, y - 3, x - 1, y + h - 3, accent());

        int yy = y;
        for (int i = 0; i < lines.length; i++) {
            int color = i == 0 ? accent() : 0xFFE2E8F0;
            graphics.text(font, lines[i], x + 2, yy, color, true);
            yy += 11;
        }
    }

    /** Пинг до сервера в миллисекундах (в одиночной игре — прочерк). */
    private static String ping(Minecraft client) {
        if (client.getConnection() == null || client.player == null) {
            return "-- ms";
        }
        PlayerInfo info = client.getConnection().getPlayerInfo(client.player.getUUID());
        return info == null ? "-- ms" : info.getLatency() + " ms";
    }

    // ============ Информация о предмете в руке ============

    private static void renderItemInfo(GuiGraphicsExtractor graphics, Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null) {
            return;
        }
        ItemStack main = player.getMainHandItem();
        if (main.isEmpty()) {
            return;
        }
        Font font = client.font;
        PrismConfig.Data cfg = PrismConfig.get();

        java.util.List<String> lines = new java.util.ArrayList<>();
        if (cfg.itemInfoName) {
            String name = main.getHoverName().getString();
            lines.add(name);
        }
        if (cfg.itemInfoDurability && main.isDamageableItem() && main.getMaxDamage() > 0) {
            int left = main.getMaxDamage() - main.getDamageValue();
            int pct = Math.round(left * 100f / main.getMaxDamage());
            lines.add(left + " / " + main.getMaxDamage() + "  (" + pct + "%)");
        }
        if (cfg.itemInfoCount && main.getCount() > 1) {
            lines.add("x" + main.getCount());
        }
        if (lines.isEmpty()) {
            return;
        }

        int w = 0;
        for (String line : lines) {
            w = Math.max(w, font.width(line));
        }
        int cx = graphics.guiWidth() / 2;
        int bottom = graphics.guiHeight() - 62;
        int top = bottom - lines.size() * 11 - 5;
        int half = w / 2 + 5;

        graphics.fill(cx - half, top, cx + half, bottom, 0x66070A12);
        graphics.fill(cx - half, top, cx - half + 2, bottom, accent());

        int y = top + 3;
        for (String line : lines) {
            graphics.centeredText(font, line, cx, y, 0xFFE2E8F0);
            y += 11;
        }
    }

    // ============ Keystrokes ============

    private static void renderKeystrokes(GuiGraphicsExtractor graphics, Minecraft client) {
        Font font = client.font;
        int size = 18;
        int gap = 2;
        int x = 8;
        int totalW = size * 3 + gap * 2;
        int sh = graphics.guiHeight();
        int y = sh - totalW - 14;

        int accent = PrismConfig.get().keysRainbow ? Colors.rainbow() : 0xFFA78BFA;

        key(graphics, font, "W", x + size + gap, y, size, client.options.keyUp.isDown(), accent);
        key(graphics, font, "A", x, y + size + gap, size, client.options.keyLeft.isDown(), accent);
        key(graphics, font, "S", x + size + gap, y + size + gap, size, client.options.keyDown.isDown(), accent);
        key(graphics, font, "D", x + (size + gap) * 2, y + size + gap, size, client.options.keyRight.isDown(), accent);

        int my = y + (size + gap) * 2;
        int half = (totalW - gap) / 2;
        String leftLabel = PrismConfig.get().keysCps ? "L " + PrismClient.attackCps() : "L";
        String rightLabel = PrismConfig.get().keysCps ? "R " + PrismClient.useCps() : "R";
        key(graphics, font, leftLabel, x, my, size, client.options.keyAttack.isDown(), accent, half);
        key(graphics, font, rightLabel, x + half + gap, my, size, client.options.keyUse.isDown(), accent, half);
        key(graphics, font, "SPACE", x, my + size + gap, size, client.options.keyJump.isDown(), accent, totalW);
    }

    private static void key(GuiGraphicsExtractor graphics, Font font, String label,
                            int x, int y, int size, boolean pressed, int accent) {
        key(graphics, font, label, x, y, size, pressed, accent, size);
    }

    private static void key(GuiGraphicsExtractor graphics, Font font, String label,
                            int x, int y, int size, boolean pressed, int accent, int width) {
        graphics.fill(x, y, x + width, y + size, pressed ? 0xAA1E1033 : 0x66070A12);
        int border = pressed ? accent : 0x33FFFFFF;
        graphics.fill(x, y, x + width, y + 1, border);
        graphics.fill(x, y + size - 1, x + width, y + size, border);
        graphics.fill(x, y, x + 1, y + size, border);
        graphics.fill(x + width - 1, y, x + width, y + size, border);

        int tw = font.width(label);
        graphics.text(font, label, x + (width - tw) / 2, y + (size - 8) / 2,
                pressed ? accent : 0xFFE2E8F0, false);
    }

    // ============ Броня над хотбаром ============

    private static void renderArmor(GuiGraphicsExtractor graphics, Minecraft client) {
        LocalPlayer player = client.player;
        ItemStack[] items = {
                player.getItemBySlot(EquipmentSlot.HEAD),
                player.getItemBySlot(EquipmentSlot.CHEST),
                player.getItemBySlot(EquipmentSlot.LEGS),
                player.getItemBySlot(EquipmentSlot.FEET),
                player.getMainHandItem()
        };

        int slots = items.length;
        int w = slots * 18;
        int x = graphics.guiWidth() / 2 - w / 2;
        int y = graphics.guiHeight() - 59;

        for (int i = 0; i < slots; i++) {
            int sx = x + i * 18;
            ItemStack stack = items[i];
            if (stack.isEmpty()) {
                continue;
            }
            graphics.fill(sx, y, sx + 17, y + 17, 0x55070A12);
            graphics.item(stack, sx, y);
            if (stack.getMaxDamage() > 0 && stack.getDamageValue() > 0) {
                float pct = 1f - (float) stack.getDamageValue() / (float) stack.getMaxDamage();
                int barColor = 0xFF000000 | Colors.hsb(pct * 0.33f, 0.9f, 1f);
                graphics.fill(sx + 2, y + 14, sx + 15, y + 16, 0xFF101018);
                graphics.fill(sx + 2, y + 14, sx + 2 + Math.round(13 * pct), y + 16, barColor);
                if (PrismConfig.get().armorPercent) {
                    String text = Math.round(pct * 100f) + "%";
                    graphics.text(client.font, text, sx + 17 - client.font.width(text), y - 9, 0xFFFFF59D, true);
                }
            }
        }
    }
}
