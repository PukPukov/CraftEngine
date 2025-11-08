package ru.mrbedrockpy.renderer.gui;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.stb.STBTTPackedchar;
import ru.mrbedrockpy.craftengine.core.util.id.RL;
import ru.mrbedrockpy.renderer.RenderInit;
import ru.mrbedrockpy.renderer.font.ComponentRenderer;
import ru.mrbedrockpy.renderer.font.GlyphAtlas;
import ru.mrbedrockpy.renderer.font.QuadBatch;
import ru.mrbedrockpy.renderer.font.StickerRegistry;
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
        shader.setUniformMatrix4f("projection", Window.scale().ortho());
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

    public void drawTexture(int x, int y, int dstW, int dstH,
                                  int u, int v, int srcW, int srcH,
                                  RL texture) {
        TextureRegion r = RenderInit.ATLAS_MANAGER.findRegion(texture);
        if (r == null || dstW <= 0 || dstH <= 0) return;

        Atlas atlas = RenderInit.ATLAS_MANAGER.get(r.atlasIndex);
        int atlasPixW = atlas.texture().width();
        int atlasPixH = atlas.texture().height();

        int regionW = Math.max(1, Math.round((r.u1 - r.u0) * atlasPixW));
        int regionH = Math.max(1, Math.round((r.v1 - r.v0) * atlasPixH));

        if (srcW <= 0) srcW = regionW;
        if (srcH <= 0) srcH = regionH;

        // подрезаем ИСТОЧНИК, а НЕ dstW/dstH
        if (u < 0) u = 0;
        if (v < 0) v = 0;
        if (u + srcW > regionW) srcW = Math.max(0, regionW - u);
        if (v + srcH > regionH) srcH = Math.max(0, regionH - v);
        if (srcW == 0 || srcH == 0) return;

        float du = r.u1 - r.u0;
        float dv = r.v1 - r.v0;

        float su0 = r.u0 + (u          / (float) regionW) * du;
        float sv0 = r.v0 + (v          / (float) regionH) * dv;
        float su1 = r.u0 + ((u + srcW) / (float) regionW) * du;
        float sv1 = r.v0 + ((v + srcH) / (float) regionH) * dv;

        // если V-инверсия нужна — свапни sv0/sv1
        // float t = sv0; sv0 = sv1; sv1 = t;

        float[] verts = {
                x,           y,           su0, sv0,
                x + dstW,    y,           su1, sv0,
                x + dstW,    y + dstH,    su1, sv1,

                x,           y,           su0, sv0,
                x + dstW,    y + dstH,    su1, sv1,
                x,           y + dstH,    su0, sv1,
        };

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferSubData(GL_ARRAY_BUFFER, 0, verts); // убедись, что VBO вмещает 6 вершин * 4 float

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


    public void drawNineSlice(int x, int y, int W, int H,
                              int u, int v, int tw, int th,
                              int l, int t, int r, int b,
                              RL texture) {
        // Подтянем фактический размер региона, если tw/th не заданы
        TextureRegion reg = RenderInit.ATLAS_MANAGER.findRegion(texture);
        if (reg == null) return;

        Atlas atlas = RenderInit.ATLAS_MANAGER.get(reg.atlasIndex);
        int atlasPixW = atlas.texture().width();
        int atlasPixH = atlas.texture().height();
        int regionW = Math.max(1, Math.round((reg.u1 - reg.u0) * atlasPixW));
        int regionH = Math.max(1, Math.round((reg.v1 - reg.v0) * atlasPixH));
        if (tw <= 0) tw = regionW;
        if (th <= 0) th = regionH;

        // Нормализуем рамки: если итоговый размер меньше суммарных рамок — пропорционально ужмём
        // по ширине
        int sumX = l + r;
        if (W < sumX && sumX > 0) {
            float sx = W / (float) sumX;
            l = Math.max(0, Math.round(l * sx));
            r = Math.max(0, W - l);
        }
        // по высоте
        int sumY = t + b;
        if (H < sumY && sumY > 0) {
            float sy = H / (float) sumY;
            t = Math.max(0, Math.round(t * sy));
            b = Math.max(0, H - t);
        }

        int cw = Math.max(0, W - l - r); // ширина центра/верх-низ
        int ch = Math.max(0, H - t - b); // высота центра/лево-право

        // ---- источники (u,v,w,h) в тайле ----
        // Углы
        int su_tl = u,            sv_tl = v,            sw_tl = l,      sh_tl = t;
        int su_tr = u + tw - r,   sv_tr = v,            sw_tr = r,      sh_tr = t;
        int su_bl = u,            sv_bl = v + th - b,   sw_bl = l,      sh_bl = b;
        int su_br = u + tw - r,   sv_br = v + th - b,   sw_br = r,      sh_br = b;

        // Рёбра
        int su_top = u + l,       sv_top = v,           sw_top = tw - l - r, sh_top = t;
        int su_bot = u + l,       sv_bot = v + th - b,  sw_bot = tw - l - r, sh_bot = b;
        int su_left = u,          sv_left = v + t,      sw_left = l,         sh_left = th - t - b;
        int su_right = u + tw - r,sv_right = v + t,     sw_right = r,        sh_right = th - t - b;

        // Центр
        int su_c = u + l,         sv_c = v + t,         sw_c = tw - l - r,   sh_c = th - t - b;

        // ---- назначения (x,y,w,h) ----
        int dx_tl = x,            dy_tl = y,            dw_tl = l,      dh_tl = t;
        int dx_tr = x + W - r,    dy_tr = y,            dw_tr = r,      dh_tr = t;
        int dx_bl = x,            dy_bl = y + H - b,    dw_bl = l,      dh_bl = b;
        int dx_br = x + W - r,    dy_br = y + H - b,    dw_br = r,      dh_br = b;

        int dx_top = x + l,       dy_top = y,           dw_top = cw,     dh_top = t;
        int dx_bot = x + l,       dy_bot = y + H - b,   dw_bot = cw,     dh_bot = b;
        int dx_left = x,          dy_left = y + t,      dw_left = l,     dh_left = ch;
        int dx_right = x + W - r, dy_right = y + t,     dw_right = r,    dh_right = ch;

        int dx_c = x + l,         dy_c = y + t,         dw_c = cw,       dh_c = ch;

        // Рисуем (функция сама отрежет нули)
        // Углы
        drawTexture(dx_tl, dy_tl, dw_tl, dh_tl, su_tl, sv_tl, tw, th, texture);
        drawTexture(dx_tr, dy_tr, dw_tr, dh_tr, su_tr, sv_tr, tw, th, texture);
        drawTexture(dx_bl, dy_bl, dw_bl, dh_bl, su_bl, sv_bl, tw, th, texture);
        drawTexture(dx_br, dy_br, dw_br, dh_br, su_br, sv_br, tw, th, texture);

        // Рёбра
        drawTexture(dx_top,   dy_top,   dw_top,   dh_top,   su_top,   sv_top,   tw, th, texture);
        drawTexture(dx_bot,   dy_bot,   dw_bot,   dh_bot,   su_bot,   sv_bot,   tw, th, texture);
        drawTexture(dx_left,  dy_left,  dw_left,  dh_left,  su_left,  sv_left,  tw, th, texture);
        drawTexture(dx_right, dy_right, dw_right, dh_right, su_right, sv_right, tw, th, texture);

        // Центр
        drawTexture(dx_c, dy_c, dw_c, dh_c, su_c, sv_c, tw, th, texture);
    }

    /** Упрощённая перегрузка: одинаковая рамка со всех сторон. */
    public void drawNineSlice(int x, int y, int W, int H,
                              int u, int v, int tw, int th,
                              int pad,
                              RL texture) {
        drawNineSlice(x, y, W, H, u, v, tw, th, pad, pad, pad, pad, texture);
    }

    public void drawRect(int x, int y, int width, int height, Color color) {
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
