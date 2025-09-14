package ru.mrbedrockpy.renderer.font;

import lombok.Getter;
import org.joml.Vector2i;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;
import ru.mrbedrockpy.renderer.graphics.FreeTextureAtlas;
import ru.mrbedrockpy.renderer.gui.DrawContext;

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
    public static final int BITMAP_W = 512;
    public static final int BITMAP_H = 512;
    private static final int FONT_SIZE = 32;

    private final FreeTextureAtlas atlas;
    private final String atlasKey;
    private Rectangle atlasRect;

    private final STBTTPackedchar.Buffer charData = STBTTPackedchar.malloc(96);
    private STBTTFontinfo fontInfo;
    private float scale;
    private int ascent;

    public FontRenderer(FreeTextureAtlas atlas, String atlasKey) {
        this.atlas = atlas;
        this.atlasKey = atlasKey;
    }

    public void init(String resourcePath) throws IOException {
        ByteBuffer fontBuffer;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) throw new IOException("Font not found: " + resourcePath);
            byte[] bytes = is.readAllBytes();
            fontBuffer = BufferUtils.createByteBuffer(bytes.length);
            fontBuffer.put(bytes).flip();
        }

        fontInfo = STBTTFontinfo.create();
        if (!stbtt_InitFont(fontInfo, fontBuffer))
            throw new IllegalStateException("Failed to init font info");

        scale = stbtt_ScaleForPixelHeight(fontInfo, FONT_SIZE);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pAscent = stack.mallocInt(1);
            stbtt_GetFontVMetrics(fontInfo, pAscent, null, null);
            ascent = (int) (pAscent.get(0) * scale);
        }

        ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            STBTTPackContext pack = STBTTPackContext.malloc(stack);
            stbtt_PackBegin(pack, bitmap, BITMAP_W, BITMAP_H, 0, 1, 0);
            stbtt_PackFontRange(pack, fontBuffer, 0, FONT_SIZE, 32, charData); // заполняет charData (x0..y1 и т.п.)
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
        if (atlasRect == null) throw new IllegalStateException("Atlas did not record rect for key: " + atlasKey);
    }

    public Vector2i getTextSize(String text) {
        int width = 0;
        int maxHeight = 0;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer x0 = stack.mallocInt(1);
            IntBuffer y0 = stack.mallocInt(1);
            IntBuffer x1 = stack.mallocInt(1);
            IntBuffer y1 = stack.mallocInt(1);
            IntBuffer advance = stack.mallocInt(1);
            IntBuffer lsb = stack.mallocInt(1);
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                stbtt_GetCodepointHMetrics(fontInfo, c, advance, lsb);
                width += (int) (advance.get(0) * scale);
                stbtt_GetCodepointBitmapBox(fontInfo, c, scale, scale, x0, y0, x1, y1);
                int height = y1.get(0) - y0.get(0);
                maxHeight = Math.max(maxHeight, height);
            }
        }
        return new Vector2i(Math.ceilDiv(width, 5), Math.ceilDiv(maxHeight, 5));
    }

    public void dispose() {
        charData.free();
    }

    public float u(float localU) {
        return (atlasRect.x / (float) atlas.getWidthPx()) + localU * (atlasRect.width / (float) atlas.getWidthPx());
    }

    public float v(float localV) {
        return (atlasRect.y / (float) atlas.getHeightPx()) + localV * (atlasRect.height / (float) atlas.getHeightPx());
    }
}