package ru.prism.vis.util;

/** Палитра Prism и радуга. */
public final class Colors {
    public static final String[] NAMES = {"Фиолет", "Циан", "Розовый", "Лайм", "Золотой", "Алый", "Белый"};
    public static final int[] RGB = {0xA78BFA, 0x22D3EE, 0xF472B6, 0xA3E635, 0xFACC15, 0xF87171, 0xF8FAFC};

    private Colors() {
    }

    public static int palette(int index) {
        return RGB[Math.floorMod(index, RGB.length)];
    }

    /** RGB -> float[3] в диапазоне 0..1 (для RenderLayer и частиц). */
    public static float[] rgbf(int rgb) {
        return new float[]{
                ((rgb >> 16) & 255) / 255f,
                ((rgb >> 8) & 255) / 255f,
                (rgb & 255) / 255f
        };
    }

    /** RGB + прозрачность -> ARGB для DrawContext. */
    public static int argb(int rgb, float alpha) {
        int a = Math.round(alpha * 255f) & 255;
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    /** Бегущая радуга, меняется каждую секунду. */
    public static int rainbow() {
        float hue = (float) ((System.currentTimeMillis() % 3000L) / 3000.0);
        return 0xFF000000 | hsb(hue, 0.85f, 1f);
    }

    public static int rainbowRgb() {
        return rainbow() & 0xFFFFFF;
    }

    /** HSB -> RGB без java.awt (чтобы было удобно использовать везде). */
    public static int hsb(float h, float s, float v) {
        h = h - (float) Math.floor(h);
        int i = (int) (h * 6f);
        float f = h * 6f - i;
        float p = v * (1f - s);
        float q = v * (1f - f * s);
        float t = v * (1f - (1f - f) * s);
        float r, g, b;
        switch (i % 6) {
            case 0 -> { r = v; g = t; b = p; }
            case 1 -> { r = q; g = v; b = p; }
            case 2 -> { r = p; g = v; b = t; }
            case 3 -> { r = p; g = q; b = v; }
            case 4 -> { r = t; g = p; b = v; }
            default -> { r = v; g = p; b = q; }
        }
        return ((int) (r * 255) << 16) | ((int) (g * 255) << 8) | (int) (b * 255);
    }
}
