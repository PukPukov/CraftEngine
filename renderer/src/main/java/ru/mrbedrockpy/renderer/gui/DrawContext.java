package ru.mrbedrockpy.renderer.gui;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.stb.STBTTPackedchar;
import ru.mrbedrockpy.renderer.font.FontRenderer;
import ru.mrbedrockpy.renderer.graphics.MatrixStack;
import ru.mrbedrockpy.renderer.graphics.Shader;
import ru.mrbedrockpy.renderer.graphics.Texture;
import ru.mrbedrockpy.renderer.window.Window;

import java.awt.*;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL46C.*;

public class DrawContext {
    private final Shader uiShader, textShader;
    private final int screenWidth;
    private final int screenHeight;
    private final int vaoId;
    private final int vboId;

    private final int fboId;
    private final int uiTextureId;

    @Getter
    private final MatrixStack matrices = new MatrixStack();
    private final FontRenderer fontRenderer;

    public DrawContext(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        uiShader   = Shader.load("ui_vertex.glsl", "ui_fragment.glsl");
        textShader = Shader.load("ui_vertex.glsl", "text_fragment.glsl");

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
        uiTextureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, uiTextureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, screenWidth, screenHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer) null);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindFramebuffer(GL_FRAMEBUFFER, fboId);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, uiTextureId, 0);
        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("UI FBO not complete");
        }
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
    }

    public void disableGL(){
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }

    public void drawTextureCentred(int x, int y, float width, float height, Texture texture) {
        drawTexture((int) (x - width / 2), (int) (y - height / 2), width, height, texture);
    }

    public void drawTexture(int x, int y, float width, float height, Texture texture) {
        float[] vertices = {
                x, y, 0.0f, 1.0f,
                x + width, y, 1.0f, 1.0f,
                x + width, y + height, 1.0f, 0.0f,

                x, y, 0.0f, 1.0f,
                x + width, y + height, 1.0f, 0.0f,
                x, y + height, 0.0f, 0.0f
        };
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);


        uiShader.use();
        uiShader.setUniformMatrix4f("projection", matrices.matrix());
        uiShader.setUniform1b("useColor", false);

        glActiveTexture(GL_TEXTURE0);
        texture.use();

        glBindVertexArray(vaoId);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);

        texture.unbind();
        uiShader.unbind();
    }

    public void drawCentredText(String text, int x, int y) {
        drawCentredText(text, x, y, 1.0f);
    }

    public void drawCentredText(String text, int x, int y, float scale) {
        Vector2i textSize = fontRenderer.size(text);
        drawText(text, x - textSize.x / 2, y - textSize.y / 2, scale);
    }

    public void drawText(String text, int x, int y) {
        drawText(text, x, y, 1.0f);
    }

    public void drawText(String text, int x, int y, float scale) {
        scale *= 0.3f;

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, fontRenderer.textureId());

        textShader.use();
        textShader.setUniformMatrix4f("projection", matrices.matrix());

        glBindVertexArray(vaoId);

        float cursorX = x;
        STBTTPackedchar.Buffer chars = fontRenderer.charData();

        float ascent = fontRenderer.ascent();
        float scaleFactor = fontRenderer.scale();
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
                    x0, y0 + h, u0, v1,
                    x0, y0, u0, v0,
                    x0 + w, y0, u1, v0,

                    x0, y0 + h, u0, v1,
                    x0 + w, y0, u1, v0,
                    x0 + w, y0 + h, u1, v1,
            };

            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
            glDrawArrays(GL_TRIANGLES, 0, 6);

            cursorX += g.xadvance() * scale;
        }

        glBindVertexArray(0);
        textShader.unbind();
    }

    public void drawRect(int x, int y, float width, float height, Color color) {
        uiShader.use();
        uiShader.setUniformMatrix4f("projection", matrices.matrix());
        uiShader.setUniform4f("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        uiShader.setUniform1b("useColor", true);

        glBindVertexArray(vaoId);

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
        uiShader.setUniform1b("useColor", false);
        uiShader.unbind();
    }

    public void cleanup() {
        glDeleteVertexArrays(vaoId);
        glDeleteBuffers(vboId);
        glDeleteFramebuffers(fboId);
        glDeleteTextures(uiTextureId);
        uiShader.dispose();
        textShader.dispose();
        fontRenderer.charData().free();
        GL11C.glDeleteTextures(fontRenderer.textureId());
    }
}
