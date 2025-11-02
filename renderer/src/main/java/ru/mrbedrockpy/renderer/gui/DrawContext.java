package ru.mrbedrockpy.renderer.gui;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.stb.STBTTPackedchar;
import ru.mrbedrockpy.craftengine.core.util.id.RL;
import ru.mrbedrockpy.renderer.RenderInit;
import ru.mrbedrockpy.renderer.font.FontRenderer;
import ru.mrbedrockpy.renderer.graphics.*;
import ru.mrbedrockpy.renderer.graphics.tex.Atlas;
import ru.mrbedrockpy.renderer.graphics.tex.GlTexture;
import ru.mrbedrockpy.renderer.graphics.tex.TextureRegion;
import ru.mrbedrockpy.renderer.resource.TextureLoader;
import ru.mrbedrockpy.renderer.util.FileLoader;
import ru.mrbedrockpy.renderer.util.graphics.ShaderUtil;
import ru.mrbedrockpy.renderer.util.graphics.TextureUtil;
import ru.mrbedrockpy.renderer.window.Window;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import static org.lwjgl.opengl.GL46C.*;

// TODO: сделать DrawContext интерфейсом, написать реализацию уменьшив бойлерплейт и оптимизировав
public class DrawContext {
    private final int vaoId;
    private final int vboId;
    private int GUI_ATLAS = -1;

    private final FreeTextureAtlas atlas = new FreeTextureAtlas();

    @Getter
    private final MatrixStack matrices = new MatrixStack();
    private final FontRenderer fontRenderer;
    private final Shader shader;
    private final int screenWidth, screenHeight;

    public DrawContext(Shader shader, int screenWidth, int screenHeight) {
        this.shader = shader;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        vaoId = glGenVertexArrays();
        vboId = glGenBuffers();
        glBindVertexArray(vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, 6 * 4 * Float.BYTES, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);
        glBindVertexArray(0);

        fontRenderer = new FontRenderer(atlas, RL.of("ui_font_main"));
        try {
            fontRenderer.init(RL.of("minecraft.ttf"));
        } catch (IOException e) {
            throw new RuntimeException("Font load error", e);
        }

        for (Map.Entry<RL, Texture> texture : RenderInit.RESOURCE_MANAGER.getTextureLoader().getAll()) {
            if (!texture.getKey().path().startsWith("gui/")) continue;
            atlas.addTexture(texture.getKey(), TextureUtil.toBufferedImage(texture.getValue()));
        }

        GlTexture guiTex = new GlTexture(atlas.getTextureId(), atlas.getWidthPx(), atlas.getHeightPx());
        Atlas ui = new Atlas(RL.of("gui"), guiTex, atlas);
        GUI_ATLAS = RenderInit.ATLAS_MANAGER.register(ui);
    }

    public void enableGL() {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_CULL_FACE);
        shader.use();
        shader.setUniformMatrix4f("projection", new Matrix4f().ortho(0.0f, screenWidth, screenHeight, 0.0f, -1.0f, 1.0f));
        shader.setUniformMatrix4f("view", new Matrix4f());
        RenderInit.ATLAS_MANAGER.uploadToShader(shader.getId(), "atlases");
    }

    public void disableGL() {
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        shader.setUniform1b("useUniformColor", false);
        shader.unbind();
    }

    public void drawTextureCentred(int x, int y, int width, int height, RL texture) {
        drawTexture(x - width / 2, y - height / 2, width, height, texture);
    }

    public void drawTexture(int x, int y, int w, int h, RL texturePath) {
        this.drawTexture(x, y, w, h, 0, 0, w, h, texturePath);
    }

    public void drawTexture(int x, int y, int w, int h, int u, int v, RL texturePath) {
        TextureRegion r = RenderInit.ATLAS_MANAGER.findRegion(texturePath);
        if (r == null) return;
        drawTexture(x, y, w, h, u, v, r.texW, r.texH, texturePath);
    }

    public void drawTexture(int x, int y, int w, int h,
                            int u, int v, int tw, int th,
                            RL texture) {
        TextureRegion r = RenderInit.ATLAS_MANAGER.findRegion(texture);
        if (r == null) return;

        // 2) реальные размеры региона в пикселях
        Atlas atlas = RenderInit.ATLAS_MANAGER.get(r.atlasIndex);
        int atlasPixW = atlas.texture().width();
        int atlasPixH = atlas.texture().height();

        int regionW = Math.max(1, Math.round((r.u1 - r.u0) * atlasPixW));
        int regionH = Math.max(1, Math.round((r.v1 - r.v0) * atlasPixH));

        // если tw/th не заданы — использовать реальные размеры региона
        if (tw <= 0) tw = regionW;
        if (th <= 0) th = regionH;

        // 3) подстраховка от выхода сабрегиона за рамки исходной текстуры
        if (u < 0) u = 0;
        if (v < 0) v = 0;
        if (u + w > tw) w = Math.max(0, tw - u);
        if (v + h > th) h = Math.max(0, th - v);
        if (w == 0 || h == 0) return;

        // 4) пересчёт сабрегиона (u,v,w,h) в нормализованные UV
        float du = r.u1 - r.u0;
        float dv = r.v1 - r.v0;

        float su0 = r.u0 + (u       / (float) tw) * du;
        float sv0 = r.v0 + (v       / (float) th) * dv;
        float su1 = r.u0 + ((u + w) / (float) tw) * du;
        float sv1 = r.v0 + ((v + h) / (float) th) * dv;

        // если координата V инвертирована — свопни
        // float t = sv0; sv0 = sv1; sv1 = t;

        // 5) заливаем в VBO
        float[] verts = {
                x,     y,     su0, sv0,
                x + w, y,     su1, sv0,
                x + w, y + h, su1, sv1,

                x,     y,     su0, sv0,
                x + w, y + h, su1, sv1,
                x,     y + h, su0, sv1,
        };

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferSubData(GL_ARRAY_BUFFER, 0, verts);

        shader.setUniform1b("useUniformColor", false);
        shader.setUniform1b("useMask", false);
        shader.setUniformMatrix4f("model", matrices.matrix());

        glBindVertexArray(vaoId);
        glVertexAttrib4f(2, 1f, 1f, 1f, 1f);
        glDisableVertexAttribArray(3);
        glVertexAttribI1i(3, r.atlasIndex);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
    }

    public void drawCentredText(String text, int x, int y) {
        drawCentredText(text, x, y, 1.0f);
    }

    public void drawCentredText(String text, int cx, int cy, float fontScale) {
        if (text == null || text.isEmpty()) return;

        Vector2f sz = fontRenderer.getTextSize(text, fontScale);

        float w = sz.x * fontScale;
        float h = sz.y * fontScale;

        int x = Math.round(cx - w / 2f);
        int y = Math.round(cy + h / 2f);

        drawText(text, x, y, fontScale);
    }


    public void drawText(String text, int x, int y) {
        drawText(text, x, y, 1.0f);
    }

    public void drawText(String text, int x, int y, float scale) {
        scale *= 0.3f;

        glBindVertexArray(vaoId);

        shader.setUniform1b("useUniformColor", true);
        shader.setUniform1b("useMask", true);
        shader.setUniform4f("uniformColor", 1.0f, 1.0f, 1.0f, 1.0f);
        shader.setUniformMatrix4f("model", matrices.matrix());

        float cursorX = x;
        STBTTPackedchar.Buffer chars = fontRenderer.getCharData();

        float ascent = fontRenderer.getAscent();
        float scaleFactor = fontRenderer.getScale();
        float baseline = ascent * scaleFactor * scale;
        float[] vertices = new float[text.length() * 24];

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            STBTTPackedchar g = chars.get(c - 32);

            float x0 = cursorX + g.xoff() * scale;
            float y0 = y + baseline + g.yoff() * scale;
            float w = (g.x1() - g.x0()) * scale;
            float h = (g.y1() - g.y0()) * scale;

            float lu0 = g.x0() / (float) FontRenderer.BITMAP_W;
            float lv0 = g.y0() / (float) FontRenderer.BITMAP_H;
            float lu1 = g.x1() / (float) FontRenderer.BITMAP_W;
            float lv1 = g.y1() / (float) FontRenderer.BITMAP_H;

            float u0 = fontRenderer.u(lu0);
            float v0 = fontRenderer.v(lv0);
            float u1 = fontRenderer.u(lu1);
            float v1 = fontRenderer.v(lv1);

            float[] vtx = {
                    x0, y0 + h, u0, v1,
                    x0, y0, u0, v0,
                    x0 + w, y0, u1, v0,

                    x0, y0 + h, u0, v1,
                    x0 + w, y0, u1, v0,
                    x0 + w, y0 + h, u1, v1,
            };
            System.arraycopy(vtx, 0, vertices, i * 24, 24);
            cursorX += g.xadvance() * scale;
        }
        glDisableVertexAttribArray(3);
        glVertexAttribI1i(3, GUI_ATLAS);

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, text.length() * 6);

        glBindVertexArray(0);

    }

    public void drawRect(int x, int y, float width, float height, Color color) {
        shader.setUniform1b("useUniformColor", true);
        shader.setUniform1b("useMask", false);
        shader.setUniform4f("uniformColor", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        shader.setUniformMatrix4f("model", matrices.matrix());

        glBindVertexArray(vaoId);
        glVertexAttrib4f(2, 1.0f, 1.0f, 1.0f, 1.0f);

        float[] vertices = {
                x, y + height, 0.0f, 1.0f,
                x, y, 0.0f, 0.0f,
                x + width, y, 1.0f, 0.0f,

                x, y + height, 0.0f, 1.0f,
                x + width, y, 1.0f, 0.0f,
                x + width, y + height, 1.0f, 1.0f,
        };

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
        glDrawArrays(GL_TRIANGLES, 0, 6);

        glBindVertexArray(0);
    }

    public void drawTextureNineSlice(int x, int y, int w, int h,
                                     int u, int v, int tw, int th,
                                     String texturePath) {

    }

    public void setShaderColor(float v, float v1, float v2, float alpha) {
        shader.setUniform4f("uniformColor", v, v1, v2, alpha);
        shader.setUniform1b("useUniformColor", !(v == 1.0f && v1 == 1.0f && v2 == 1.0f && alpha == 1.0f));
    }
}
