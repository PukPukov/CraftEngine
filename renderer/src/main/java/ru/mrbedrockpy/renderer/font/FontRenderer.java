package ru.mrbedrockpy.renderer.font;

import lombok.Getter;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;
import ru.mrbedrockpy.craftengine.core.util.id.RL;
import ru.mrbedrockpy.renderer.graphics.FreeTextureAtlas;
import ru.mrbedrockpy.renderer.gui.DrawContext;
import ru.mrbedrockpy.renderer.window.Window;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL46C.*;
import static org.lwjgl.stb.STBTruetype.*;

@Getter
public class FontRenderer {
    public static final int BITMAP_W = 512, BITMAP_H = 512;
    private static final int FONT_SIZE = 32;

    private final FreeTextureAtlas atlas;
    private final RL atlasKey;
    private Rectangle atlasRect;

    // было final -> убрал
    private STBTTPackedchar.Buffer charData;
    private STBTTFontinfo fontInfo;
    private ByteBuffer fontData;        // <-- ДЕРЖИМ ЭТО ПОЛЕ, пока жив шрифт!
    private float scale;
    private int ascent;

    public FontRenderer(FreeTextureAtlas atlas, RL atlasKey) {
        this.atlas = atlas;
        this.atlasKey = atlasKey;
    }

    public void init(RL fontId) throws IOException {
        if (fontInfo != null || charData != null) dispose();

        // Построим classpath-путь: assets/<ns>/fonts/<path>.ttf
        String cp = toFontClasspath(fontId);

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = getClass().getClassLoader();

        try (InputStream is = cl.getResourceAsStream(cp)) {
            if (is == null) {
                throw new IOException("Font not found on classpath: " + cp + " (from " + fontId + ")");
            }
            byte[] bytes = is.readAllBytes();
            fontData = BufferUtils.createByteBuffer(bytes.length);
            fontData.put(bytes).flip();
        }

        fontInfo = STBTTFontinfo.create();
        if (!stbtt_InitFont(fontInfo, fontData)) {
            throw new IllegalStateException("Failed to init font info");
        }

        scale = stbtt_ScaleForPixelHeight(fontInfo, FONT_SIZE);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pAscent = stack.mallocInt(1);
            stbtt_GetFontVMetrics(fontInfo, pAscent, null, null);
            ascent = (int) (pAscent.get(0) * scale);
        }

        // (пере)аллоцируем charData тут, а не в поле по умолчанию
        charData = STBTTPackedchar.malloc(96);

        // билдим атлас глифов
        ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            STBTTPackContext pack = STBTTPackContext.malloc(stack);
            stbtt_PackBegin(pack, bitmap, BITMAP_W, BITMAP_H, 0, 1, 0);
            stbtt_PackFontRange(pack, fontData, 0, FONT_SIZE, 32, charData);
            stbtt_PackEnd(pack);
        }

        BufferedImage img = new BufferedImage(BITMAP_W, BITMAP_H, BufferedImage.TYPE_INT_ARGB);
        int[] argb = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        bitmap.rewind();
        for (int y = 0; y < BITMAP_H; y++) {
            for (int x = 0; x < BITMAP_W; x++) {
                int a = bitmap.get() & 0xFF;
                argb[y * BITMAP_W + x] = (a << 24) | 0x00FFFFFF;
            }
        }

        if (!atlas.contains(atlasKey)) {
            atlas.addTexture(atlasKey, img);
        }
        atlasRect = atlas.getUvMap().get(atlasKey);
        if (atlasRect == null) {
            throw new IllegalStateException("Atlas did not record rect for key: " + atlasKey);
        }
    }

    /** Превращает логический RL(ns:path) в classpath: assets/<ns>/fonts/<path>.ttf */
    private static String toFontClasspath(RL id) {
        String ns = id.namespace();
        String p  = id.path().replace('\\', '/');

        // допускаем, что могли передать «лишнее» — аккуратно уберём
        if (p.startsWith("assets/")) p = p.substring("assets/".length());
        if (p.startsWith(ns + "/")) p = p.substring(ns.length() + 1);
        if (p.startsWith("fonts/")) p = p.substring("fonts/".length());
        if (p.endsWith(".ttf"))     p = p.substring(0, p.length() - 4);

        return "assets/" + ns + "/fonts/" + p + ".ttf";
    }

    public Vector2f measureTextPx(String text) {
        if (text == null || text.isEmpty()) return new Vector2f(0, 0);
        if (fontInfo == null || fontData == null) throw new IllegalStateException("Font not initialized");

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer ascentBuf  = stack.mallocInt(1);
            IntBuffer descentBuf = stack.mallocInt(1);
            IntBuffer gapBuf     = stack.mallocInt(1);
            stbtt_GetFontVMetrics(fontInfo, ascentBuf, descentBuf, gapBuf);

            float a = ascentBuf.get(0), d = descentBuf.get(0), g = gapBuf.get(0);
            if (a == 0 && d == 0) throw new IllegalStateException("Font metrics are zero (dangling fontInfo/fontData)");

            float s = this.scale;
            float lineHeightPx = (a - d + g) * s;

            IntBuffer advance = stack.mallocInt(1);
            IntBuffer lsb     = stack.mallocInt(1);

            float widthPx = 0f; int prev = 0;
            for (int cp : text.codePoints().toArray()) {
                stbtt_GetCodepointHMetrics(fontInfo, cp, advance, lsb);
                widthPx += advance.get(0) * s;
                if (prev != 0) widthPx += stbtt_GetCodepointKernAdvance(fontInfo, prev, cp) * s;
                prev = cp;
            }
            return new Vector2f(Math.max(0, widthPx), Math.max(0, lineHeightPx));
        }
    }

    public Vector2f getTextSize(String text, float renderScale) {
        Vector2f px = measureTextPx(text);
        float sx = Window.scaledWidth()  / (float) Window.getWidth();
        float sy = Window.scaledHeight() / (float) Window.getHeight();
        return new Vector2f(px.x * renderScale * sx, px.y * renderScale * sy);
    }

    public void dispose() {
        if (charData != null) { charData.free(); charData = null; }
        fontInfo = null;
        fontData = null;
        atlasRect = null;
    }

    public float u(float localU) {
        return (atlasRect.x / (float) atlas.getWidthPx()) + localU * (atlasRect.width / (float) atlas.getWidthPx());
    }

    public float v(float localV) {
        return (atlasRect.y / (float) atlas.getHeightPx()) + localV * (atlasRect.height / (float) atlas.getHeightPx());
    }

}