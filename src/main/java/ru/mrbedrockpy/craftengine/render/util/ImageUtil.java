package ru.mrbedrockpy.craftengine.render.util;

import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

public final class ImageUtil {

    public static ByteBuffer toByteBuffer(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();

        int[] pixels = new int[w * h];
        img.getRGB(0, 0, w, h, pixels, 0, w);

        ByteBuffer buffer = BufferUtils.createByteBuffer(w * h * 4);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = pixels[y * w + x];

                buffer.put((byte) ((argb >> 16) & 0xFF)); // R
                buffer.put((byte) ((argb >>  8) & 0xFF)); // G
                buffer.put((byte) ( argb        & 0xFF)); // B
                buffer.put((byte) ((argb >> 24) & 0xFF)); // A
            }
        }
        buffer.flip();
        return buffer;
    }

    public static BufferedImage load(String file) throws IOException {
        try (var in = FileLoader.loadStream(file)) {
            return ImageIO.read(in);
        }
    }
}