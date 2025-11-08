package ru.mrbedrockpy.renderer.font;

import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;
import ru.mrbedrockpy.craftengine.core.util.id.RL;
import ru.mrbedrockpy.renderer.graphics.FreeTextureAtlas;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBTruetype.*;

public final class GlyphAtlas {
    public static final int PACK_W = 512, PACK_H = 512;
    public static final int FONT_PX = 40;
    private static final int FIRST = 32, COUNT = 96; // ASCII (32..127)


    private final FreeTextureAtlas atlas;
    private final RL atlasKey;

    private STBTTFontinfo fontInfo;
    private STBTTPackedchar.Buffer charData;
    private ByteBuffer fontData;
    private float scale;
    private int ascentPx;

    private Rectangle atlasRect;

    public GlyphAtlas(FreeTextureAtlas atlas, RL atlasKey) {
        this.atlas = atlas;
        this.atlasKey = atlasKey;
    }

    public void init(RL fontId) throws Exception {
        dispose();

        String cp = toFontClasspath(fontId);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = getClass().getClassLoader();
        try (InputStream is = cl.getResourceAsStream(cp)) {
            if (is == null) throw new IllegalStateException("Font not found: " + cp);
            byte[] bytes = is.readAllBytes();
            fontData = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
            fontData.put(bytes).flip();
        }

        fontInfo = STBTTFontinfo.create();
        if (!stbtt_InitFont(fontInfo, fontData)) throw new IllegalStateException("stbtt_InitFont failed");

        scale = stbtt_ScaleForPixelHeight(fontInfo, FONT_PX);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer a = stack.mallocInt(1);
            stbtt_GetFontVMetrics(fontInfo, a, null, null);
            ascentPx = Math.round(a.get(0) * scale);
        }

        // пакуем
        charData = STBTTPackedchar.malloc(COUNT);
        ByteBuffer mono = ByteBuffer.allocateDirect(PACK_W * PACK_H);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            STBTTPackContext ctx = STBTTPackContext.malloc(stack);
            stbtt_PackBegin(ctx, mono, PACK_W, PACK_H, 0, 1, 0);
            stbtt_PackFontRange(ctx, fontData, 0, FONT_PX, FIRST, charData);
            stbtt_PackEnd(ctx);
        }

        // альфа → ARGB
        BufferedImage img = new BufferedImage(PACK_W, PACK_H, BufferedImage.TYPE_INT_ARGB);
        int[] argb = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
        mono.rewind();
        for (int i = 0; i < argb.length; i++) {
            int a = mono.get() & 0xFF;
            argb[i] = (a << 24) | 0x00FFFFFF; // белая маска
        }

        if (!atlas.contains(atlasKey)) atlas.addTexture(atlasKey, img);
        atlasRect = atlas.getUvMap().get(atlasKey);
        if (atlasRect == null) throw new IllegalStateException("No atlas rect for " + atlasKey);
    }

    public void dispose() {
        if (charData != null) { charData.free(); charData = null; }
        fontInfo = null;
        fontData = null;
        atlasRect = null;
    }

    public int ascentPx() { return ascentPx; }
    public STBTTPackedchar.Buffer chars() { return charData; }

    public float remapU(float local) {
        return (atlasRect.x + local * atlasRect.width) / (float) atlas.getWidthPx();
    }
    public float remapV(float local) {
        return (atlasRect.y + local * atlasRect.height) / (float) atlas.getHeightPx();
    }

    public static String toFontClasspath(RL id) {
        String ns = id.namespace();
        String p  = id.path().replace('\\', '/');
        if (p.startsWith("assets/")) p = p.substring(7);
        if (p.startsWith(ns + "/")) p = p.substring(ns.length() + 1);
        if (p.startsWith("fonts/")) p = p.substring(6);
        if (p.endsWith(".ttf"))     p = p.substring(0, p.length() - 4);
        return "assets/" + ns + "/fonts/" + p + ".ttf";
    }

    public static int toIndex(int codePoint) {
        int idx = codePoint - FIRST;
        return (idx < 0 || idx >= COUNT) ? ('?' - FIRST) : idx;
    }
}