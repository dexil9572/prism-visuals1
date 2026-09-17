package ru.prism.vis.render;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.ModuleManager;
import ru.prism.vis.util.Colors;

/**
 * Подсветка блока под прицелом: контур + полупрозрачная заливка.
 * Регистрируется на WorldRenderEvents.LAST.
 */
public final class OverlayRenderer {
    private OverlayRenderer() {
    }

    public static void renderLast(WorldRenderContext context) {
        if (!ModuleManager.BLOCK_OVERLAY.isEnabled()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.crosshairTarget == null
                || client.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            return;
        }

        BlockHitResult hit = (BlockHitResult) client.crosshairTarget;
        BlockPos pos = hit.getBlockPos();
        Camera camera = context.camera();
        Vec3d cam = camera.getPos();

        // координаты рендера — относительно камеры
        Box box = new Box(pos).expand(0.002).offset(-cam.x, -cam.y, -cam.z);

        MatrixStack matrices = context.matrixStack();
        VertexConsumerProvider consumers = context.consumers();
        if (matrices == null || consumers == null) {
            return;
        }

        PrismConfig.Data cfg = PrismConfig.get();
        float[] c = Colors.rgbf(Colors.palette(cfg.overlayColor));

        // Контур
        VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());
        WorldRenderer.drawBox(matrices, lines, box, c[0], c[1], c[2], 1.0f);

        // Заливка
        if (cfg.overlayFill && cfg.overlayAlpha > 0) {
            float alpha = cfg.overlayAlpha / 100f;
            VertexConsumer quads = consumers.getBuffer(RenderLayer.getDebugQuads());
            Matrix4f matrix = matrices.peek().getPositionMatrix();
            fillBox(quads, matrix, box, c[0], c[1], c[2], alpha);
        }
    }

    private static void fillBox(VertexConsumer v, Matrix4f m, Box b,
                                float r, float g, float bl, float a) {
        float x1 = (float) b.minX, y1 = (float) b.minY, z1 = (float) b.minZ;
        float x2 = (float) b.maxX, y2 = (float) b.maxY, z2 = (float) b.maxZ;

        quad(v, m, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, r, g, bl, a); // низ
        quad(v, m, x1, y2, z1, x2, y2, z1, x2, y2, z2, x1, y2, z2, r, g, bl, a); // верх
        quad(v, m, x1, y1, z1, x2, y1, z1, x2, y2, z1, x1, y2, z1, r, g, bl, a); // север
        quad(v, m, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, r, g, bl, a); // юг
        quad(v, m, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, r, g, bl, a); // запад
        quad(v, m, x2, y1, z1, x2, y1, z2, x2, y2, z2, x2, y2, z1, r, g, bl, a); // восток
    }

    private static void quad(VertexConsumer v, Matrix4f m,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             float r, float g, float b, float a) {
        v.vertex(m, x1, y1, z1).color(r, g, b, a);
        v.vertex(m, x2, y2, z2).color(r, g, b, a);
        v.vertex(m, x3, y3, z3).color(r, g, b, a);
        v.vertex(m, x4, y4, z4).color(r, g, b, a);
    }
}
