package ru.mrbedrockpy.renderer.gui;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.stb.STBTTPackedchar;
import ru.mrbedrockpy.renderer.font.FontRenderer;
import ru.mrbedrockpy.renderer.graphics.*;
import ru.mrbedrockpy.renderer.util.FileLoader;
import ru.mrbedrockpy.renderer.util.graphics.ShaderUtil;
import ru.mrbedrockpy.renderer.window.Window;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static org.lwjgl.opengl.GL46C.*;

public class DrawContext {
    private final Shader uiShader;
    private final int screenWidth;
    private final int screenHeight;
    private final int vaoId;
    private final int vboId;

    private final FreeTextureAtlas atlas = new FreeTextureAtlas();

    @Getter
    private final MatrixStack matrices = new MatrixStack();
    private final FontRenderer fontRenderer;

    public DrawContext(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        uiShader = ShaderUtil.load("ui_vertex.glsl", "ui_fragment.glsl");

        matrices.set(new Matrix4f().ortho(0.0f, screenWidth, screenHeight, 0.0f, -1.0f, 1.0f));

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

        fontRenderer = new FontRenderer(atlas, "ui_font_main");
        try {
            fontRenderer.init("minecraft.ttf");
        } catch (IOException e) {
            throw new RuntimeException("Font load error", e);
        }
    }

    public void enableGL() {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_CULL_FACE);
        atlas.use();
        uiShader.use();
        uiShader.setUniformMatrix4f("projection", matrices.matrix());
    }

    public void disableGL() {
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        uiShader.unbind();
    }


    public void drawTextureCentred(int x, int y, int width, int height, String texture) {
        drawTexture(x - width / 2, y - height / 2, width, height, texture);
    }

    public void drawTexture(int x, int y, int w, int h, String texturePath) {
        this.drawTexture(x, y, w, h, 0, 0, w, h, texturePath);
    }

    public void drawTexture(int x, int y, int w, int h, int u, int v,String texturePath) {
       this.drawTexture(x, y, w, h, u, v, w, h, texturePath);
    }

    public void drawTexture(int x, int y, int w, int h,
                            int u, int v, int tw, int th,
                            String texturePath) {
        if (!atlas.contains(texturePath)) {
            BufferedImage img = FileLoader.loadImage(texturePath);
            atlas.addTexture(texturePath, img);
        }

        float[] uv = atlas.getNormalizedUvs(texturePath);
        // порядок: 0:(x0,y0), 1:(x1,y0), 2:(x1,y1), 3:(x0,y1)
        float uL = uv[0], vT = uv[1];
        float uR = uv[4], vB = uv[5];
        float du = uR - uL;
        float dv = vB - vT;

        // нормализуем координаты относительно размеров текстуры
        float su0 = uL + (u / (float) tw) * du;
        float sv0 = vT + (v / (float) th) * dv;
        float su1 = uL + ((u + w) / (float) tw) * du;
        float sv1 = vT + ((v + h) / (float) th) * dv;

        // если ось V перевёрнута
        // float tmp = sv0; sv0 = sv1; sv1 = tmp;

        float[] verts = {
                x, y, su0, sv0,
                x + w, y, su1, sv0,
                x + w, y + h, su1, sv1,

                x, y, su0, sv0,
                x + w, y + h, su1, sv1,
                x, y + h, su0, sv1,
        };

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferSubData(GL_ARRAY_BUFFER, 0, verts);

        uiShader.setUniform1i("uiTexture", 0);
        uiShader.setUniform1b("useUniformColor", false);
        uiShader.setUniform1b("useMask", false);

        glVertexAttrib4f(2, 1f, 1f, 1f, 1f);
        glBindVertexArray(vaoId);
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

        uiShader.setUniform1b("useUniformColor", true);
        uiShader.setUniform1b("useMask", true);
        uiShader.setUniform4f("uniformColor", 1.0f, 1.0f, 1.0f, 1.0f);
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
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glDrawArrays(GL_TRIANGLES, 0, text.length() * 6);

        glBindVertexArray(0);

    }

    public void drawRect(int x, int y, float width, float height, Color color) {
        uiShader.setUniform1b("useUniformColor", true);
        uiShader.setUniform1b("useMask", false);
        uiShader.setUniform4f("uniformColor", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

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
}
