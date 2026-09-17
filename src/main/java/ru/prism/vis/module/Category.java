package ru.prism.vis.module;

/** Категории модулей в Prism GUI. */
public enum Category {
    RENDER("Рендер", 0xA78BFA),
    HUD("HUD", 0x22D3EE),
    WORLD("Мир", 0x4ADE80),
    PLAYER("Игрок", 0xFACC15);

    public final String label;
    public final int color;

    Category(String label, int color) {
        this.label = label;
        this.color = color;
    }
}
