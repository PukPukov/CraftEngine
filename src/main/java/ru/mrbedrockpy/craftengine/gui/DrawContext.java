package ru.mrbedrockpy.craftengine.gui;

import org.joml.Matrix4f;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.stb.STBTruetype;
import ru.mrbedrockpy.craftengine.graphics.Shader;
import ru.mrbedrockpy.craftengine.graphics.Texture;
import ru.mrbedrockpy.craftengine.gui.font.FontRenderer;
import ru.mrbedrockpy.craftengine.gui.font.Glyph;

import java.awt.*;
import java.io.IOException;

import static org.lwjgl.opengl.GL46C.*;

// TODO: Add text rendering capabilities
public class DrawContext {
    private final Shader uiShader, textShader;
    private final int screenWidth;
    private final int screenHeight;

    private int vaoId;
    private int vboId;

    private final Matrix4f projection;
    private final FontRenderer fontRenderer;

    public DrawContext(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        uiShader = Shader.load("ui_vertex.glsl", "ui_fragment.glsl");
        textShader = Shader.load("ui_vertex.glsl", "text_fragment.glsl");

        projection = new Matrix4f().ortho(0.0f, screenWidth, screenHeight, 0.0f, -1.0f, 1.0f);

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

        fontRenderer = new FontRenderer();
        try {
            fontRenderer.init("UbuntuMono.ttf");
        } catch (IOException e) {
            throw new RuntimeException("Font load error", e);
        }
    }

    public void drawTexture(int x, int y, float width, float height, Texture texture) {
        float[] vertices = {
                x,          y,          0.0f, 1.0f,
                x + width,  y,          1.0f, 1.0f,
                x + width,  y + height, 1.0f, 0.0f,

                x,          y,          0.0f, 1.0f,
                x + width,  y + height, 1.0f, 0.0f,
                x,          y + height, 0.0f, 0.0f
        };
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_CULL_FACE);

        uiShader.use();
        uiShader.setUniformMatrix4f("projection", projection);
        uiShader.setUniform1b("useColor", false);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getId());

        glBindVertexArray(vaoId);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);

        texture.unbind();
        uiShader.unbind();

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
    }

    public void drawText(String text, float x, float y) {
        drawText(text, x, y, 1.0f);
    }
    public void drawText(String text, float x, float y, float scale) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glDisable(GL_CULL_FACE);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, fontRenderer.getTextureId());

        textShader.use();
        textShader.setUniformMatrix4f("projection", projection);

        glBindVertexArray(vaoId);

        float cursorX = x;
        STBTTPackedchar.Buffer chars = fontRenderer.getCharData();

        float ascent = fontRenderer.getAscent();
        float scaleFactor = fontRenderer.getScale();
        float baseline = ascent * scaleFactor * scale;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c < 32 || c >= 128) continue;

            STBTTPackedchar g = chars.get(c - 32);

            float x0 = cursorX + g.xoff() * scale;
            float y0 = y + baseline + g.yoff() * scale;
            float w = (g.x1() - g.x0()) * scale;
            float h = (g.y1() - g.y0()) * scale;

            float u0 = g.x0() / 512.0f;
            float v0 = g.y0() / 512.0f;
            float u1 = g.x1() / 512.0f;
            float v1 = g.y1() / 512.0f;

            float[] vertices = {
                    x0,     y0 + h, u0, v1,
                    x0,     y0,     u0, v0,
                    x0 + w, y0,     u1, v0,

                    x0,     y0 + h, u0, v1,
                    x0 + w, y0,     u1, v0,
                    x0 + w, y0 + h, u1, v1,
            };

            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
            glDrawArrays(GL_TRIANGLES, 0, 6);

            cursorX += g.xadvance() * scale;
        }

        glBindVertexArray(0);
        textShader.unbind();

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
    }

    public void drawRect(int x, int y, float width, float height, Color color) {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_CULL_FACE);

        uiShader.use();
        uiShader.setUniformMatrix4f("projection", projection);
        uiShader.setUniform4f("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        uiShader.setUniform1b("useColor", true);

        glBindVertexArray(vaoId);

        float[] vertices = {
                x,         y + height, 0.0f, 1.0f,
                x,         y,          0.0f, 0.0f,
                x + width, y,          1.0f, 0.0f,

                x,         y + height, 0.0f, 1.0f,
                x + width, y,          1.0f, 0.0f,
                x + width, y + height, 1.0f, 1.0f,
        };

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
        glDrawArrays(GL_TRIANGLES, 0, 6);

        glBindVertexArray(0);
        uiShader.setUniform1b("useColor", false);
        uiShader.unbind();

        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
    }

    public void cleanup() {
        glDeleteVertexArrays(vaoId);
        glDeleteBuffers(vboId);
        uiShader.dispose();
        textShader.dispose();
        fontRenderer.getCharData().free();
        glDeleteTextures(fontRenderer.getTextureId());
    }
}
