package ru.prism.vis.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import ru.prism.vis.PrismClient;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.ModuleManager;
import ru.prism.vis.util.Colors;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Весь HUD мода рисуется здесь.
 * Вызывается из InGameHudMixin в конце рендера HUD.
 */
public final class HudRenderer {
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");

    private HudRenderer() {
    }

    public static void render(DrawContext ctx, RenderTickCounter counter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null || client.options.hudHidden) {
            return;
        }
        int sw = ctx.getScaledWindowWidth();
        int sh = ctx.getScaledWindowHeight();

        if (ModuleManager.CROSSHAIR.isEnabled()) {
            renderCrosshair(ctx, client, sw, sh);
        }
        if (ModuleManager.HUD_INFO.isEnabled()) {
            renderInfo(ctx, client);
        }
        if (ModuleManager.KEYSTROKES.isEnabled()) {
            renderKeystrokes(ctx, client, sh);
        }
        if (ModuleManager.ARMOR_HUD.isEnabled()) {
            renderArmor(ctx, client, sw, sh);
        }
        if (ModuleManager.TOGGLE_SPRINT.isEnabled() && PrismConfig.get().sprintToggled) {
            String s = "Спринт [ВКЛ]";
            int w = client.textRenderer.getWidth(s);
            drawText(ctx, client.textRenderer, s, sw / 2 - w / 2, sh - 76, accent(), true);
        }
    }

    private static int accent() {
        return PrismConfig.get().hudRainbow ? Colors.rainbow() : Colors.argb(Colors.palette(0), 1f);
    }

    // ============ Кастомный прицел ============

    private static void renderCrosshair(DrawContext ctx, MinecraftClient client, int sw, int sh) {
        ClientPlayerEntity player = client.player;
        PrismConfig.Data cfg = PrismConfig.get();

        int cx = sw / 2;
        int cy = sh / 2;

        // смотрим НЕ в режиме зрителя и не на экран отладки
        double speed = Math.hypot(player.getVelocity().x, player.getVelocity().z);
        float cooldown = player.getAttackCooldownProgress(0.0f);

        int gap = cfg.crossGap + (int) Math.round(speed * 10.0) + (int) ((1f - cooldown) * 3f);
        int size = cfg.crossSize;
        int color = cfg.crossRainbow ? Colors.rainbow() : Colors.argb(Colors.palette(cfg.crossColor), 1f);

        // центральная точка
        ctx.fill(cx - 1, cy - 1, cx + 1, cy + 1, color);
        // лучи
        ctx.fill(cx - gap - size, cy, cx - gap, cy + 1, color);
        ctx.fill(cx + gap, cy, cx + gap + size, cy + 1, color);
        ctx.fill(cx, cy - gap - size, cx + 1, cy - gap, color);
        ctx.fill(cx, cy + gap, cx + 1, cy + gap + size, color);

        // полоса перезарядки атаки
        if (cooldown < 1f) {
            int w = size * 4;
            int y = cy + gap + size + 3;
            ctx.fill(cx - w / 2, y, cx + w / 2, y + 2, 0x66000000);
            ctx.fill(cx - w / 2, y, cx - w / 2 + (int) (w * cooldown), y + 2, color);
        }
    }

    // ============ Информационная панель ============

    private static void renderInfo(DrawContext ctx, MinecraftClient client) {
        TextRenderer tr = client.textRenderer;
        ClientPlayerEntity player = client.player;
        BlockPos pos = player.getBlockPos();
        String biome = client.world != null
                ? client.world.getBiome(pos).getKey().map(k -> k.getValue().getPath()).orElse("?")
                : "?";

        String[] lines = {
                "PRISM " + PrismClient.VERSION,
                client.getCurrentFps() + " FPS",
                String.format("XYZ  %.1f  %.1f  %.1f", player.getX(), player.getY(), player.getZ()),
                "F " + player.getHorizontalFacing().asString() + "  ·  " + biome,
                "CPS  " + PrismClient.attackCps() + " | " + PrismClient.useCps(),
                LocalTime.now().format(CLOCK)
        };

        int w = 0;
        for (String line : lines) {
            w = Math.max(w, tr.getWidth(line));
        }

        int x = 6;
        int y = 6;
        int h = lines.length * 11 + 6;

        ctx.fill(x - 3, y - 3, x + w + 6, y + h - 3, 0x66070A12);
        ctx.fill(x - 3, y - 3, x - 1, y + h - 3, accent());

        int yy = y;
        for (int i = 0; i < lines.length; i++) {
            int color = i == 0 ? accent() : 0xFFE2E8F0;
            drawText(ctx, tr, lines[i], x + 2, yy, color, true);
            yy += 11;
        }
    }

    // ============ Keystrokes ============

    private static void renderKeystrokes(DrawContext ctx, MinecraftClient client, int sh) {
        TextRenderer tr = client.textRenderer;
        int size = 18;
        int gap = 2;
        int x = 8;
        int totalW = size * 3 + gap * 2;
        int y = sh - totalW - 14;

        boolean rainbow = PrismConfig.get().keysRainbow;
        int accent = rainbow ? Colors.rainbow() : 0xFFA78BFA;

        key(ctx, tr, "W", x + size + gap, y, size, client.options.forwardKey.pressed, accent);
        key(ctx, tr, "A", x, y + size + gap, size, client.options.leftKey.pressed, accent);
        key(ctx, tr, "S", x + size + gap, y + size + gap, size, client.options.backKey.pressed, accent);
        key(ctx, tr, "D", x + (size + gap) * 2, y + size + gap, size, client.options.rightKey.pressed, accent);

        int my = y + (size + gap) * 2;
        int half = (totalW - gap) / 2;
        key(ctx, tr, "L", x, my, size, client.options.attackKey.pressed, accent, half, accent);
        key(ctx, tr, "R", x + half + gap, my, size, client.options.useKey.pressed, accent, half, accent);
        key(ctx, tr, "SPACE", x, my + size + gap, size, client.options.jumpKey.pressed, accent, totalW, accent);
    }

    private static void key(DrawContext ctx, TextRenderer tr, String label,
                            int x, int y, int size, boolean pressed, int accent) {
        key(ctx, tr, label, x, y, size, pressed, accent, size, accent);
    }

    private static void key(DrawContext ctx, TextRenderer tr, String label,
                            int x, int y, int size, boolean pressed, int textColor, int width, int accent) {
        ctx.fill(x, y, x + width, y + size, pressed ? 0xAA1E1033 : 0x66070A12);
        int border = pressed ? accent : 0x33FFFFFF;
        ctx.fill(x, y, x + width, y + 1, border);
        ctx.fill(x, y + size - 1, x + width, y + size, border);
        ctx.fill(x, y, x + 1, y + size, border);
        ctx.fill(x + width - 1, y, x + width, y + size, border);

        int tw = tr.getWidth(label);
        drawText(ctx, tr, label, x + (width - tw) / 2, y + (size - 8) / 2,
                pressed ? accent : 0xFFE2E8F0, false);
    }

    // ============ Броня над хотбаром ============

    private static void renderArmor(DrawContext ctx, MinecraftClient client, int sw, int sh) {
        ClientPlayerEntity player = client.player;
        ItemStack[] items = new ItemStack[5];
        items[0] = player.getInventory().armor.get(3);
        items[1] = player.getInventory().armor.get(2);
        items[2] = player.getInventory().armor.get(1);
        items[3] = player.getInventory().armor.get(0);
        items[4] = player.getMainHandStack();

        int slots = 5;
        int w = slots * 18;
        int x = sw / 2 - w / 2;
        int y = sh - 59;

        for (int i = 0; i < slots; i++) {
            int sx = x + i * 18;
            ItemStack stack = items[i];
            if (stack.isEmpty()) {
                continue;
            }
            ctx.fill(sx, y, sx + 17, y + 17, 0x55070A12);
            ctx.drawItem(stack, sx, y);
            if (stack.getMaxDamage() > 0 && stack.getDamage() > 0) {
                float pct = 1f - (float) stack.getDamage() / (float) stack.getMaxDamage();
                int barColor = 0xFF000000 | Colors.hsb(pct * 0.33f, 0.9f, 1f);
                ctx.fill(sx + 2, y + 14, sx + 15, y + 16, 0xFF101018);
                ctx.fill(sx + 2, y + 14, sx + 2 + Math.round(13 * pct), y + 16, barColor);
            }
        }
    }

    // ============ Утилита ============

    private static void drawText(DrawContext ctx, TextRenderer tr, String s, int x, int y, int color, boolean shadow) {
        if (shadow) {
            ctx.drawTextWithShadow(tr, s, x, y, color);
        } else {
            ctx.drawText(tr, s, x, y, color, false);
        }
    }
}
