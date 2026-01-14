package ru.mrbedrockpy.renderer.gui;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import ru.mrbedrockpy.craftengine.core.util.id.RL;
import ru.mrbedrockpy.renderer.RenderInit;
import ru.mrbedrockpy.renderer.font.ComponentRenderer;
import ru.mrbedrockpy.renderer.font.GlyphAtlas;
import ru.mrbedrockpy.renderer.font.QuadBatch;
import ru.mrbedrockpy.renderer.font.StickerRegistry;
import ru.mrbedrockpy.renderer.graphics.*;
import ru.mrbedrockpy.renderer.graphics.tex.Atlas;
import ru.mrbedrockpy.renderer.graphics.tex.TextureRegion;
import ru.mrbedrockpy.renderer.util.graphics.TextureUtil;
import ru.mrbedrockpy.renderer.window.Window;

import java.awt.*;
import ru.mrbedrockpy.craftengine.core.util.lang.Component;

import static org.lwjgl.opengl.GL46C.*;

// TODO: сделать DrawContext интерфейсом, написать реализацию уменьшив бойлерплейт и оптимизировав
public class DrawContext{
    private final int vaoId;
    private final int vboId;
    private int GUI_ATLAS = -1;

    private final FreeTextureAtlas atlas = new FreeTextureAtlas();

    @Getter
    private final MatrixStack matrices = new MatrixStack();
    @Getter
    private final ComponentRenderer fontRenderer;
    private final Shader shader;

    public DrawContext(Shader shader) {
        this.shader = shader;
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
        System.out.println("all textures: "+RenderInit.RESOURCE_MANAGER.getTextureLoader().getAll().size());
        for (var e : RenderInit.RESOURCE_MANAGER.getTextureLoader().getAll()) {
            RL key = e.getKey();
            if (!key.path().startsWith("gui/") && !key.path().startsWith("sticker/")) continue;
            atlas.addTexture(key, TextureUtil.toBufferedImage(e.getValue()));
        }
        StickerRegistry.registerAllStickers(atlas);
        GlyphAtlas glyphAtlas = new GlyphAtlas(atlas, RL.of("ui_font_main"));
        try {
            glyphAtlas.init(RL.of("minecraft.ttf"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        fontRenderer = new ComponentRenderer(glyphAtlas, StickerRegistry.INSTANCE);
        GUI_ATLAS = RenderInit.ATLAS_MANAGER.register(atlas);
    }

    public void enableGL() {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_CULL_FACE);
        shader.use();
        shader.setUniformMatrix4f("projection", Window.getScaleManager().ortho());
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
        this.drawTexture(x, y, w, h, 0, 0, texturePath);
    }

    public void drawTexture(int x, int y, int w, int h, int u, int v, RL texturePath) {
        drawTexture(x, y, w, h, u, v, 0, 0, texturePath);
    }

    public void drawTexture(int x, int y, int dstW, int dstH, int u, int v, int srcW, int srcH, RL texture) {
        TextureRegion r = RenderInit.ATLAS_MANAGER.findRegion(texture);
        if (r == null || dstW <= 0 || dstH <= 0) return;

        int regionW = r.texW;
        int regionH = r.texH;

        if (srcW <= 0) srcW = regionW;
        if (srcH <= 0) srcH = regionH;

        float du = r.u1 - r.u0;
        float dv = r.v1 - r.v0;

        float su0 = r.u0 + (u          / (float) regionW) * du;
        float su1 = r.u0 + ((u + srcW) / (float) regionW) * du;

        float sv0 = r.v0 + (v / (float) regionH) * dv;;;
        float sv1 = r.v0 + (v + srcH    / (float) regionH) * dv;;

        float[] verts = {
                x,           y,           su0, sv0,
                x + dstW,    y,           su1, sv0,
                x + dstW,    y + dstH,    su1, sv1,

                x,           y,           su0, sv0,
                x + dstW,    y + dstH,    su1, sv1,
                x,           y + dstH,    su0, sv1,
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


    public void drawRect(int x, int y, int width, int height, Color color) {
        shader.setUniform1b("useUniformColor", true);
        shader.setUniform1b("useMask", false);
        shader.setUniform4fColor("uniformColor", color);
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



    public void setShaderColor(float v, float v1, float v2, float alpha) {
        shader.setUniform4f("uniformColor", v, v1, v2, alpha);
        shader.setUniform1b("useUniformColor", !(v == 1.0f && v1 == 1.0f && v2 == 1.0f && alpha == 1.0f));
    }

    public void drawCentredText(String text, int cx, int cy) {
        if (text == null || text.isEmpty()) return;
        Component comp = Component.literal(text);

        Vector2f sz = fontRenderer.getTextSize(comp);
        float x = cx - sz.x / 2f;
        float baselineY = cy - sz.y / 2f;

        fontRenderer.draw(textBatch(), comp, x, baselineY, 0xFFFFFFFF, null);
    }

    public void drawText(String text, int x, int y) {
        if (text == null || text.isEmpty()) return;
        fontRenderer.draw(textBatch(), Component.literal(text), x, y, 0xFFFFFFFF, null);
    }

    public void drawText(Component component, int x, int y, int rgba) {
        if (component == null) return;
        fontRenderer.draw(textBatch(), component, x, y, rgba, null);
    }

    private QuadBatch textBatch() {
        return new QuadBatch() {
            // ГЛИФЫ: маска + uniform-цвет
            @Override
            public void maskedQuad(float x0, float y0, float x1, float y1,
                                   float u0, float v0, float u1, float v1,
                                   int rgba, float italicSkew) {

                shader.setUniform1b("useUniformColor", true);
                shader.setUniform1b("useMask", true);
                shader.setUniform4f("uniformColor",
                        ((rgba>>>16)&0xFF)/255f,
                        ((rgba>>> 8)&0xFF)/255f,
                        ( rgba      &0xFF)/255f,
                        ((rgba>>>24)&0xFF)/255f);
                shader.setUniformMatrix4f("model", matrices.matrix());


                float[] verts = {
                        x0, y0, u0, v0,
                        x1, y0, u1, v0,
                        x1,  y1, u1, v1,
                        x0, y0, u0, v0,
                        x1,  y1, u1, v1,
                        x0,  y1, u0, v1,
                };

                glBindVertexArray(vaoId);
                glBindBuffer(GL_ARRAY_BUFFER, vboId);
                glBufferSubData(GL_ARRAY_BUFFER, 0, verts);
                glDisableVertexAttribArray(3);
                glVertexAttribI1i(3, GUI_ATLAS);
                glVertexAttrib4f(2, 1f,1f,1f,1f);
                glDrawArrays(GL_TRIANGLES, 0, 6);
                glBindVertexArray(0);
            }

            // СТИКЕРЫ: цвет из текстуры
            @Override
            public void texturedQuad(float x0, float y0, float x1, float y1,
                                     float u0, float v0, float u1, float v1) {

                shader.setUniform1b("useUniformColor", false);
                shader.setUniform1b("useMask", false);
                shader.setUniformMatrix4f("model", matrices.matrix());

                float[] verts = {
                        x0, y0, u0, v0,
                        x1, y0, u1, v0,
                        x1,  y1, u1, v1,

                        x0, y0, u0, v0,
                        x1,  y1, u1, v1,
                        x0,  y1, u0, v1,
                };

                glBindVertexArray(vaoId);
                glBindBuffer(GL_ARRAY_BUFFER, vboId);
                glBufferSubData(GL_ARRAY_BUFFER, 0, verts);
                glDisableVertexAttribArray(3);
                glVertexAttribI1i(3, GUI_ATLAS);
                glVertexAttrib4f(2, 1f,1f,1f,1f);
                glDrawArrays(GL_TRIANGLES, 0, 6);
                glBindVertexArray(0);
            }

            @Override
            public void solidQuad(float x0, float y0, float x1, float y1, int rgba) {
                shader.setUniform1b("useUniformColor", true);
                shader.setUniform1b("useMask", false);
                shader.setUniform4f("uniformColor",
                        ((rgba>>>16)&0xFF)/255f,
                        ((rgba>>> 8)&0xFF)/255f,
                        ( rgba      &0xFF)/255f,
                        ((rgba>>>24)&0xFF)/255f);
                shader.setUniformMatrix4f("model", matrices.matrix());

                float[] verts = {
                        x0, y0, 0f,0f,
                        x1, y0, 1f,0f,
                        x1, y1, 1f,1f,

                        x0, y0, 0f,0f,
                        x1, y1, 1f,1f,
                        x0, y1, 0f,1f,
                };

                glBindVertexArray(vaoId);
                glBindBuffer(GL_ARRAY_BUFFER, vboId);
                glBufferSubData(GL_ARRAY_BUFFER, 0, verts);
                glDisableVertexAttribArray(3);
                glVertexAttribI1i(3, GUI_ATLAS);
                glVertexAttrib4f(2, 1f,1f,1f,1f);
                glDrawArrays(GL_TRIANGLES, 0, 6);
                glBindVertexArray(0);
            }
        };
    }



}
