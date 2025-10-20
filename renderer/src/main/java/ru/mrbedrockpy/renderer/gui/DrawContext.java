package ru.mrbedrockpy.renderer.gui;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.stb.STBTTPackedchar;
import ru.mrbedrockpy.renderer.RenderInit;
import ru.mrbedrockpy.renderer.font.FontRenderer;
import ru.mrbedrockpy.renderer.graphics.*;
import ru.mrbedrockpy.renderer.graphics.tex.Atlas;
import ru.mrbedrockpy.renderer.graphics.tex.GlTexture;
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

    public DrawContext(Shader shader, int screenWidth, int screenHeight) {
        this.shader = shader;
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

        for (Map.Entry<String, Texture> texture : RenderInit.RESOURCE_MANAGER.getTextureLoader().getAll()) {
            if (!texture.getKey().startsWith("gui/")) continue;
            atlas.addTexture(texture.getKey(), TextureUtil.toBufferedImage(texture.getValue()));
        }

        GlTexture guiTex = new GlTexture(atlas.getTextureId());
        Atlas ui = new Atlas("gui", guiTex, atlas);
        GUI_ATLAS = RenderInit.ATLAS_MANAGER.register(ui);
    }

    public void enableGL() {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_CULL_FACE);
        shader.use();
        shader.setUniformMatrix4f("projection", matrices.matrix());
        shader.setUniform1b("useView", false);
        RenderInit.ATLAS_MANAGER.uploadToShader(shader.getId(), "atlases");
    }

    public void disableGL() {
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        shader.unbind();
    }


    public void drawTextureCentred(int x, int y, int width, int height, String texture) {
        drawTexture(x - width / 2, y - height / 2, width, height, texture);
    }

    public void drawTexture(int x, int y, int w, int h, String texturePath) {
//        Texture texture = TextureUtil.fromBufferedImage();
        this.drawTexture(x, y, w, h, 0, 0, w, h, texturePath);
    }

    public void drawTexture(int x, int y, int w, int h, int u, int v, String texturePath) {
        this.drawTexture(x, y, w, h, u, v, w, h, texturePath);
    }

    public void drawTexture(int x, int y, int w, int h,
                            int u, int v, int tw, int th,
                            String texturePath) {
        texturePath = texturePath.endsWith(".png") ? texturePath.substring(0, texturePath.length() - 4) : texturePath;
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


        shader.setUniform1b("useUniformColor", false);
        shader.setUniform1b("useMask", false);

        glBindVertexArray(vaoId);

        glVertexAttrib4f(2, 1f, 1f, 1f, 1f);
        glDisableVertexAttribArray(3);
        glVertexAttribI1i(3, GUI_ATLAS);

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
}
