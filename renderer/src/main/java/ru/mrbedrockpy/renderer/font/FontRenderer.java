package ru.mrbedrockpy.renderer.font;

import lombok.Getter;
import org.joml.Vector2i;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTTPackContext;
import org.lwjgl.stb.STBTTPackedchar;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.opengl.GL46C.*;
import static org.lwjgl.stb.STBTruetype.*;

@Getter
public class FontRenderer {
    private static final int BITMAP_W = 512;
    private static final int BITMAP_H = 512;
    private static final int FONT_SIZE = 32;

    private int textureId;
    private final STBTTPackedchar.Buffer charData = STBTTPackedchar.malloc(96);

    private ByteBuffer fontBuffer;
    private STBTTFontinfo fontInfo;
    private float scale;
    private int ascent;

    public void init(String resourcePath) throws IOException {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Font not found: " + resourcePath);
            }

            byte[] bytes = is.readAllBytes();
            fontBuffer = BufferUtils.createByteBuffer(bytes.length);
            fontBuffer.put(bytes).flip();
        }

        fontInfo = STBTTFontinfo.create();
        if (!stbtt_InitFont(fontInfo, fontBuffer)) {
            throw new IllegalStateException("Could not init font info");
        }

        scale = stbtt_ScaleForPixelHeight(fontInfo, FONT_SIZE);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pAscent = stack.mallocInt(1);
            stbtt_GetFontVMetrics(fontInfo, pAscent, null, null);
            ascent = pAscent.get(0);
        }

        ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            STBTTPackContext pack = STBTTPackContext.malloc(stack);
            stbtt_PackBegin(pack, bitmap, BITMAP_W, BITMAP_H, 0, 1, 0);
            stbtt_PackFontRange(pack, fontBuffer, 0, FONT_SIZE, 32, charData);
            stbtt_PackEnd(pack);
        }

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, BITMAP_W, BITMAP_H, 0, GL_RED, GL_UNSIGNED_BYTE, bitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);
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
                if (c < 32 || c >= 128) continue;

                stbtt_GetCodepointHMetrics(fontInfo, c, advance, lsb);
                width += (int) (advance.get(0) * scale);

                stbtt_GetCodepointBitmapBox(fontInfo, c, scale, scale, x0, y0, x1, y1);
                int height = y1.get(0) - y0.get(0);
                maxHeight = Math.max(maxHeight, height);
            }
        }

        return new Vector2i(width, maxHeight);
    }

    public void dispose() {
        glDeleteTextures(textureId);
        charData.free();
    }
}