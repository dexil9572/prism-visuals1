package ru.prism.vis.ui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import ru.prism.vis.PrismClient;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.Module;
import ru.prism.vis.module.ModuleManager;
import ru.prism.vis.module.Setting;
import ru.prism.vis.util.Colors;

import java.util.ArrayList;
import java.util.List;

/**
 * Prism GUI — открывается на Right Shift.
 * ЛКМ по карточке — вкл/выкл, ПКМ — выбрать для настройки.
 */
public class PrismScreen extends Screen {
    private int selected;
    private final List<Zone> zones = new ArrayList<>();

    public PrismScreen() {
        super(Text.literal("Prism Visuals"));
    }

    private record Zone(int x, int y, int w, int h, int moduleIndex, Runnable action) {
        boolean contains(int px, int py) {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        this.zones.clear();

        // фон: тёмный градиент поверх игры
        ctx.fillGradient(0, 0, this.width, this.height, 0xE6040612, 0xF20B0317);

        int centerX = this.width / 2;
        drawCentered(ctx, "PRISM VISUALS", centerX, 12, Colors.rainbow());
        drawCentered(ctx, "клиентский визуальный мод · v" + PrismClient.VERSION, centerX, 26, 0xFF94A3B8);

        List<Module> mods = ModuleManager.modules();
        if (selected >= mods.size()) {
            selected = 0;
        }

        // сетка карточек 3 × N
        int cols = 3;
        int cardW = 188;
        int cardH = 54;
        int gap = 8;
        int gridW = cols * cardW + (cols - 1) * gap;
        int startX = centerX - gridW / 2;
        int startY = 42;

        for (int i = 0; i < mods.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            drawCard(ctx, mods.get(i), startX + col * (cardW + gap), startY + row * (cardH + gap), cardW, cardH, i);
        }

        // панель настроек выбранного модуля
        int rows = (mods.size() + cols - 1) / cols;
        int panelY = startY + rows * (cardH + gap) + 4;
        drawSettings(ctx, mods.get(selected), startX, panelY, gridW);

        // нижняя панель
        int btnW = button(ctx, "Открыть конфиг (config/prism.json)", startX, this.height - 24, () -> {
            try {
                Util.getOperatingSystem().open(PrismConfig.file().getParent().toFile());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        ctx.drawTextWithShadow(this.textRenderer,
                "ЛКМ — вкл/выкл · ПКМ — выбрать · Esc — закрыть",
                startX + btnW + 16, this.height - 23, 0xFF64748B);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawCard(DrawContext ctx, Module m, int x, int y, int w, int h, int index) {
        boolean on = m.isEnabled();
        boolean isSelected = index == selected;

        ctx.fill(x, y, x + w, y + h, on ? 0x9918102E : 0x990A0D18);
        border(ctx, x, y, w, h, isSelected ? Colors.rainbow() : (on ? 0xFFA78BFA : 0x33FFFFFF));
        // цветная полоса категории
        ctx.fill(x, y, x + 2, y + h, 0xFF000000 | m.category.color);

        ctx.drawTextWithShadow(this.textRenderer, m.name, x + 8, y + 7, on ? 0xFFFFFFFF : 0xFF94A3B8);
        String desc = this.textRenderer.trimToWidth(m.description, w - 16);
        ctx.drawTextWithShadow(this.textRenderer, desc, x + 8, y + 21, 0xFFA5B4C8);

        // тумблер
        int tw = 26;
        int th = 12;
        int tx = x + w - tw - 8;
        int ty = y + h - th - 8;
        ctx.fill(tx, ty, tx + tw, ty + th, on ? 0xFF7C3AED : 0xFF1E293B);
        int knobX = on ? tx + tw - 9 : tx + 2;
        ctx.fill(knobX, ty + 2, knobX + 7, ty + th - 2, 0xFFFFFFFF);

        String state = on ? "ON" : "OFF";
        ctx.drawTextWithShadow(this.textRenderer, state, x + 8, y + h - 15,
                on ? 0xFFA78BFA : 0xFF475569);
        ctx.drawTextWithShadow(this.textRenderer, m.category.label, x + 30, y + h - 15,
                0xFF000000 | m.category.color);

        zones.add(new Zone(x, y, w, h, index, m::toggle));
    }

    private void drawSettings(DrawContext ctx, Module m, int x, int y, int w) {
        int h = 56;
        ctx.fill(x, y, x + w, y + h, 0x99101824);
        border(ctx, x, y, w, h, 0x33FFFFFF);
        ctx.drawTextWithShadow(this.textRenderer, "Настройки: " + m.name, x + 8, y + 6, 0xFFE2E8F0);

        if (m.settings.isEmpty()) {
            ctx.drawTextWithShadow(this.textRenderer,
                    "У этого модуля нет настроек — просто включи его.", x + 8, y + 24, 0xFF64748B);
            return;
        }

        int sx = x + 8;
        for (Setting s : m.settings) {
            String value = s.display();
            ctx.drawTextWithShadow(this.textRenderer, s.label, sx, y + 20, 0xFFA5B4C8);
            int buttonY = y + 33;
            int minusW = button(ctx, "[-]", sx, buttonY, () -> s.click(true));
            int valueW = this.textRenderer.getWidth(value);
            ctx.drawTextWithShadow(this.textRenderer, value, sx + minusW + 6, buttonY + 1, 0xFFFFFFFF);
            int plusX = sx + minusW + 6 + valueW + 6;
            int plusW = button(ctx, "[+]", plusX, buttonY, () -> s.click(false));
            sx += (plusX - sx) + plusW + 22;
            if (sx > x + w - 70) {
                break; // не влезает — модулю хватит и видимых
            }
        }
    }

    /** Рисует кнопку и возвращает её ширину. */
    private int button(DrawContext ctx, String label, int x, int y, Runnable action) {
        int w = this.textRenderer.getWidth(label) + 10;
        ctx.fill(x, y, x + w, y + 11, 0xFF1E293B);
        border(ctx, x, y, w, 11, 0x44FFFFFF);
        ctx.drawTextWithShadow(this.textRenderer, label, x + 5, y + 2, 0xFFE2E8F0);
        zones.add(new Zone(x, y, w, 11, selected, action));
        return w;
    }

    private void border(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x, y, x + w, y + 1, color);
        ctx.fill(x, y + h - 1, x + w, y + h, color);
        ctx.fill(x, y, x + 1, y + h, color);
        ctx.fill(x + w - 1, y, x + w, y + h, color);
    }

    private void drawCentered(DrawContext ctx, String text, int centerX, int y, int color) {
        ctx.drawCenteredTextWithShadow(this.textRenderer, text, centerX, y, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) mouseX;
        int my = (int) mouseY;
        for (Zone zone : new ArrayList<>(zones)) {
            if (zone.contains(mx, my)) {
                if (button == 0) {
                    zone.action().run();
                } else if (button == 1) {
                    selected = zone.moduleIndex();
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        PrismConfig.save();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
