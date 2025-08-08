package ru.mrbedrockpy.renderer.gui;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.stb.STBTTPackedchar;
import ru.mrbedrockpy.renderer.font.FontRenderer;
import ru.mrbedrockpy.renderer.graphics.*;
import ru.mrbedrockpy.renderer.util.FileLoader;

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

    private final int fboId;
    private final FreeTextureAtlas atlas = new FreeTextureAtlas(512, 512);

    @Getter
    private final MatrixStack matrices = new MatrixStack();
    private final FontRenderer fontRenderer;

    public DrawContext(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        uiShader   = Shader.load("ui_vertex.glsl", "ui_fragment.glsl");

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

        fboId = glGenFramebuffers();
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        fontRenderer = new FontRenderer();
        try {
            fontRenderer.init("minecraft.ttf");
        } catch (IOException e) {
            throw new RuntimeException("Font load error", e);
        }
    }

    public void enableGL(){
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_CULL_FACE);
        atlas.bind();
        uiShader.use();
        uiShader.setUniformMatrix4f("projection", matrices.matrix());
    }

    public void disableGL(){
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        uiShader.unbind();
//        try {
//            atlas.saveAsPng("atlas.png");
//        } catch (Exception ignored){}
    }


    public void drawTextureCentred(int x, int y, int width, int height, String texture) {
        drawTexture(x - width / 2, y - height / 2, width, height, texture);
    }


    public void drawTexture(int x, int y, int w, int h, String texturePath) {
        if(!atlas.contains(texturePath)) {
            BufferedImage img = FileLoader.loadImage(texturePath);
            atlas.addTexture(texturePath, img);
        }

        float[] uv = atlas.getNormalizedUvs(texturePath);
        float[] verts = {
                x,   y,   uv[0], uv[1],
                x+w, y,   uv[2], uv[3],
                x+w, y+h, uv[4], uv[5],

                x,   y,   uv[0], uv[1],
                x+w, y+h, uv[4], uv[5],
                x,   y+h, uv[6], uv[7],
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

    public void drawCentredText(String text, int x, int y, float scale) {
        Vector2i textSize = fontRenderer.getTextSize(text);
        drawText(text, x - textSize.x / 2, y - textSize.y / 2, scale);
    }

    public void drawText(String text, int x, int y) {
        drawText(text, x, y, 1.0f);
    }

    public void drawText(String text, int x, int y, float scale) {
        scale *= 0.3f;

        glBindVertexArray(vaoId);

        fontRenderer.use();

        uiShader.setUniform1b("useUniformColor", true);
        uiShader.setUniform1b("useMask", true);
        uiShader.setUniform4f("uniformColor", 1.0f, 1.0f, 1.0f, 1.0f);
        float cursorX = x;
        STBTTPackedchar.Buffer chars = fontRenderer.charData();

        float ascent = fontRenderer.ascent();
        float scaleFactor = fontRenderer.scale();
        float baseline = ascent * scaleFactor * scale;
        float[] vertices = new float[text.length() * 24];

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            STBTTPackedchar g = chars.get(c - 32);

            float x0 = cursorX + g.xoff() * scale;
            float y0 = y + baseline + g.yoff() * scale;
            float w = (g.x1() - g.x0()) * scale;
            float h = (g.y1() - g.y0()) * scale;

            float u0 = g.x0() / 512.0f;
            float v0 = g.y0() / 512.0f;
            float u1 = g.x1() / 512.0f;
            float v1 = g.y1() / 512.0f;

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

        fontRenderer.unbind();
        atlas.bind();
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

    public void cleanup() {
        glDeleteVertexArrays(vaoId);
        glDeleteBuffers(vboId);
        glDeleteFramebuffers(fboId);
        uiShader.dispose();
        fontRenderer.charData().free();
    }
}
