package ru.prism.vis.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
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
        super(Component.literal("Prism Visuals"));
    }

    private record Zone(int x, int y, int w, int h, int moduleIndex, Runnable action) {
        boolean contains(int px, int py) {
            return px >= x && px <= x + w && py >= y && py <= y + h;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        this.zones.clear();
        Font font = this.font;

        // фон: тёмный градиент поверх игры
        graphics.fillGradient(0, 0, this.width, this.height, 0xE6040612, 0xF20B0317);

        int centerX = this.width / 2;
        graphics.centeredText(font, "PRISM VISUALS", centerX, 12, Colors.rainbow());
        graphics.centeredText(font, "клиентский визуальный мод · v" + PrismClient.VERSION, centerX, 26, 0xFF94A3B8);

        List<Module> mods = ModuleManager.modules();
        if (selected >= mods.size()) {
            selected = 0;
        }

        // сетка карточек: колонки и высота подстраиваются под экран
        int gap = 8;
        int cols = mods.size() > 12 ? 4 : 3;
        int rows = (mods.size() + cols - 1) / cols;
        int cardW = Math.min(188, (this.width - 32 - (cols - 1) * gap) / cols);
        int cardH = Math.min(54, Math.max(34, (this.height - 156 - (rows - 1) * gap) / rows));
        int gridW = cols * cardW + (cols - 1) * gap;
        int startX = centerX - gridW / 2;
        int startY = 42;

        for (int i = 0; i < mods.size(); i++) {
            int col = i % cols;
            int row = i / cols;
            drawCard(graphics, mods.get(i), startX + col * (cardW + gap), startY + row * (cardH + gap), cardW, cardH, i);
        }

        // панель настроек выбранного модуля
        int panelY = startY + rows * (cardH + gap) + 4;
        drawSettings(graphics, mods.get(selected), startX, panelY, gridW);

        // нижняя панель
        int btnW = button(graphics, "Открыть конфиг (config/prism.json)", startX, this.height - 24, () -> {
            try {
                Util.getPlatform().openFile(PrismConfig.file().getParent().toFile());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        graphics.text(font, "ЛКМ — вкл/выкл · ПКМ — выбрать · Esc — закрыть",
                startX + btnW + 16, this.height - 23, 0xFF64748B, true);

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private void drawCard(GuiGraphicsExtractor graphics, Module m, int x, int y, int w, int h, int index) {
        Font font = this.font;
        boolean on = m.isEnabled();
        boolean isSelected = index == selected;

        graphics.fill(x, y, x + w, y + h, on ? 0x9918102E : 0x990A0D18);
        border(graphics, x, y, w, h, isSelected ? Colors.rainbow() : (on ? 0xFFA78BFA : 0x33FFFFFF));
        // цветная полоса категории
        graphics.fill(x, y, x + 2, y + h, 0xFF000000 | m.category.color);

        boolean compact = h < 44;
        graphics.text(font, m.name, x + 8, y + (compact ? 5 : 7), on ? 0xFFFFFFFF : 0xFF94A3B8, true);
        if (!compact) {
            String desc = font.plainSubstrByWidth(m.description, w - 16);
            graphics.text(font, desc, x + 8, y + 21, 0xFFA5B4C8, true);
        }

        // тумблер
        int tw = 26;
        int th = 12;
        int tx = x + w - tw - 8;
        int ty = y + h - th - 8;
        graphics.fill(tx, ty, tx + tw, ty + th, on ? 0xFF7C3AED : 0xFF1E293B);
        int knobX = on ? tx + tw - 9 : tx + 2;
        graphics.fill(knobX, ty + 2, knobX + 7, ty + th - 2, 0xFFFFFFFF);

        String state = on ? "ON" : "OFF";
        graphics.text(font, state, x + 8, y + h - 13, on ? 0xFFA78BFA : 0xFF475569, true);
        graphics.text(font, m.category.label, x + 30, y + h - 13, 0xFF000000 | m.category.color, true);

        zones.add(new Zone(x, y, w, h, index, m::toggle));
    }

    private void drawSettings(GuiGraphicsExtractor graphics, Module m, int x, int y, int w) {
        Font font = this.font;
        int h = 56;
        graphics.fill(x, y, x + w, y + h, 0x99101824);
        border(graphics, x, y, w, h, 0x33FFFFFF);
        graphics.text(font, "Настройки: " + m.name, x + 8, y + 6, 0xFFE2E8F0, true);

        if (m.settings.isEmpty()) {
            graphics.text(font, "У этого модуля нет настроек — просто включи его.",
                    x + 8, y + 24, 0xFF64748B, true);
            return;
        }

        int sx = x + 8;
        for (Setting s : m.settings) {
            String value = s.display();
            graphics.text(font, s.label, sx, y + 20, 0xFFA5B4C8, true);
            int buttonY = y + 33;
            int minusW = button(graphics, "[-]", sx, buttonY, () -> s.click(true));
            int valueW = font.width(value);
            graphics.text(font, value, sx + minusW + 6, buttonY + 1, 0xFFFFFFFF, true);
            int plusX = sx + minusW + 6 + valueW + 6;
            int plusW = button(graphics, "[+]", plusX, buttonY, () -> s.click(false));
            sx += (plusX - sx) + plusW + 22;
            if (sx > x + w - 70) {
                break; // не влезает — модулю хватит и видимых
            }
        }
    }

    /** Рисует кнопку и возвращает её ширину. */
    private int button(GuiGraphicsExtractor graphics, String label, int x, int y, Runnable action) {
        int w = this.font.width(label) + 10;
        graphics.fill(x, y, x + w, y + 11, 0xFF1E293B);
        border(graphics, x, y, w, 11, 0x44FFFFFF);
        graphics.text(this.font, label, x + 5, y + 2, 0xFFE2E8F0, true);
        zones.add(new Zone(x, y, w, 11, selected, action));
        return w;
    }

    private void border(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int color) {
        graphics.fill(x, y, x + w, y + 1, color);
        graphics.fill(x, y + h - 1, x + w, y + h, color);
        graphics.fill(x, y, x + 1, y + h, color);
        graphics.fill(x + w - 1, y, x + w, y + h, color);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int mx = (int) event.x();
        int my = (int) event.y();
        for (Zone zone : new ArrayList<>(zones)) {
            if (zone.contains(mx, my)) {
                if (event.button() == 0) {
                    zone.action().run();
                } else if (event.button() == 1) {
                    selected = zone.moduleIndex();
                }
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public void onClose() {
        PrismConfig.save();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
