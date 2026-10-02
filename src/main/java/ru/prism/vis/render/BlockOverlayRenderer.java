package ru.prism.vis.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import ru.prism.vis.config.PrismConfig;
import ru.prism.vis.module.ModuleManager;
import ru.prism.vis.util.Colors;

/**
 * Подсветка блока под прицелом: контур + полупрозрачная заливка.
 * Регистрируется на LevelRenderEvents.BEFORE_BLOCK_OUTLINE.
 */
public final class BlockOverlayRenderer {
    private BlockOverlayRenderer() {
    }

    /** @return false — отменяет ванильный контур блока (мы рисуем свой). */
    public static boolean render(LevelRenderContext context, BlockOutlineRenderState outline) {
        if (!ModuleManager.BLOCK_OVERLAY.isEnabled() || outline == null) {
            return true;
        }
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return true;
        }

        PrismConfig.Data cfg = PrismConfig.get();
        int rgb = Colors.palette(cfg.overlayColor);
        float red = ((rgb >> 16) & 255) / 255f;
        float green = ((rgb >> 8) & 255) / 255f;
        float blue = (rgb & 255) / 255f;

        Vec3 camera = context.levelState().cameraRenderState.pos;
        BlockPos pos = outline.pos();

        PoseStack pose = context.poseStack();
        pose.pushPose();
        pose.translate(pos.getX() - camera.x, pos.getY() - camera.y, pos.getZ() - camera.z);

        // Контур блока
        int lineColor = ARGB.colorFromFloat(1.0f, red, green, blue);
        context.submitNodeCollector().submitShapeOutline(
                pose, Shapes.block(), RenderTypes.lines(), lineColor, 2.0f, false);

        // Полупрозрачная заливка
        if (cfg.overlayFill && cfg.overlayAlpha > 0) {
            AABB box = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0).inflate(0.002);
            int fillColor = ARGB.colorFromFloat(cfg.overlayAlpha / 100f, red, green, blue);
            context.submitNodeCollector().submitCustomGeometry(pose, RenderTypes.debugFilledBox(),
                    (p, buffer) -> fillBox(p, buffer, box, fillColor));
        }

        pose.popPose();
        return false;
    }

    private static void fillBox(PoseStack.Pose pose, VertexConsumer buffer, AABB box, int color) {
        float x1 = (float) box.minX;
        float y1 = (float) box.minY;
        float z1 = (float) box.minZ;
        float x2 = (float) box.maxX;
        float y2 = (float) box.maxY;
        float z2 = (float) box.maxZ;

        // низ / верх
        quad(buffer, pose, x1, y1, z1, x2, y1, z1, x2, y1, z2, x1, y1, z2, color);
        quad(buffer, pose, x1, y2, z1, x1, y2, z2, x2, y2, z2, x2, y2, z1, color);
        // север / юг
        quad(buffer, pose, x1, y1, z1, x1, y2, z1, x2, y2, z1, x2, y1, z1, color);
        quad(buffer, pose, x1, y1, z2, x2, y1, z2, x2, y2, z2, x1, y2, z2, color);
        // запад / восток
        quad(buffer, pose, x1, y1, z1, x1, y1, z2, x1, y2, z2, x1, y2, z1, color);
        quad(buffer, pose, x2, y1, z1, x2, y2, z1, x2, y2, z2, x2, y1, z2, color);
    }

    private static void quad(VertexConsumer buffer, PoseStack.Pose pose,
                             float ax, float ay, float az,
                             float bx, float by, float bz,
                             float cx, float cy, float cz,
                             float dx, float dy, float dz,
                             int color) {
        buffer.addVertex(pose, ax, ay, az).setColor(color);
        buffer.addVertex(pose, bx, by, bz).setColor(color);
        buffer.addVertex(pose, cx, cy, cz).setColor(color);
        buffer.addVertex(pose, dx, dy, dz).setColor(color);
    }
}
