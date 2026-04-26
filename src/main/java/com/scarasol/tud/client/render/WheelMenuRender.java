package com.scarasol.tud.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.scarasol.tud.init.TudShaders;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import org.joml.Matrix4f;

/**
 * @author Scarasol
 */
public class WheelMenuRender {

    public static void drawWheel(GuiGraphics gg,
                                 float cx, float cy,
                                 float outerRadius, float innerRadius,
                                 int segments,
                                 int selectedIndex,
                                 int baseArgb,
                                 int highlightArgb,
                                 int lineArgb,
                                 float lineWidthPx,
                                 float aaPx) {

        if (segments <= 0) {
            return;
        }

        ShaderInstance shader = TudShaders.WHEEL_MENU_SHADER;
        if (shader == null) {
            return;
        }

        float[] base = argbToRgba(baseArgb);
        float[] highlight = argbToRgba(highlightArgb);
        float[] line = argbToRgba(lineArgb);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        RenderSystem.setShader(() -> shader);

        setVec2(shader, "u_Size", outerRadius * 2f, outerRadius * 2f);
        set1f(shader, "u_RadiusOuter", outerRadius);
        set1f(shader, "u_RadiusInner", Math.max(0f, innerRadius));
        set1f(shader, "u_AA", aaPx);

        setVec4(shader, "u_BaseColor", base[0], base[1], base[2], base[3]);
        setVec4(shader, "u_HighlightColor", highlight[0], highlight[1], highlight[2], highlight[3]);
        setVec4(shader, "u_LineColor", line[0], line[1], line[2], line[3]);
        set1f(shader, "u_LineWidthPx", lineWidthPx);

        set1f(shader, "u_Segments", (float) segments);
        set1f(shader, "u_Selected", selectedIndex >= 0 ? (float) selectedIndex : -1f);

        float left = cx - outerRadius;
        float top = cy - outerRadius;
        float right = cx + outerRadius;
        float bottom = cy + outerRadius;

        Matrix4f mat = gg.pose().last().pose();

        BufferBuilder buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        buf.vertex(mat, left, top, 0).uv(0, 0).endVertex();
        buf.vertex(mat, left, bottom, 0).uv(0, 1).endVertex();
        buf.vertex(mat, right, bottom, 0).uv(1, 1).endVertex();
        buf.vertex(mat, right, top, 0).uv(1, 0).endVertex();

        BufferUploader.drawWithShader(buf.end());

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    private static float[] argbToRgba(int argb) {
        float a = ((argb >>> 24) & 255) / 255f;
        float r = ((argb >>> 16) & 255) / 255f;
        float g = ((argb >>> 8) & 255) / 255f;
        float b = (argb & 255) / 255f;
        return new float[]{r, g, b, a};
    }

    private static void set1f(ShaderInstance shader, String name, float v) {
        var u = shader.getUniform(name);
        if (u != null) {
            u.set(v);
        }
    }

    private static void setVec2(ShaderInstance shader, String name, float x, float y) {
        var u = shader.getUniform(name);
        if (u != null) {
            u.set(x, y);
        }
    }

    private static void setVec4(ShaderInstance shader, String name, float x, float y, float z, float w) {
        var u = shader.getUniform(name);
        if (u != null) {
            u.set(x, y, z, w);
        }
    }
}
