package ru.mrbedrockpy.renderer.util;

import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ImageUtil {

    private ImageUtil() {}                // утилитарный класс, экземпляры не нужны

    /**
     * Конвертирует {@link BufferedImage} (ARGB) → {@link ByteBuffer} (RGBA).
     * Буфер уже позиционирован на 0 и готов к передаче в OpenGL.
     */
    public static ByteBuffer toByteBuffer(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();

        int[] pixels = new int[w * h];
        img.getRGB(0, 0, w, h, pixels, 0, w);

        ByteBuffer buffer = BufferUtils.createByteBuffer(w * h * 4);

        // BufferedImage хранит пиксель как 0xAARRGGBB (big-endian).
        // В OpenGL хотим RGBA (каждый компонент по байту).
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = pixels[y * w + x];

                buffer.put((byte) ((argb >> 16) & 0xFF)); // R
                buffer.put((byte) ((argb >>  8) & 0xFF)); // G
                buffer.put((byte) ( argb        & 0xFF)); // B
                buffer.put((byte) ((argb >> 24) & 0xFF)); // A
            }
        }
        buffer.flip();                                   // позиционируем на 0
        return buffer;
    }

    /** Быстро загружает PNG/JPG и т. д. в {@link BufferedImage}. */
    public static BufferedImage load(Path file) throws IOException {
        try (var in = Files.newInputStream(file)) {
            return ImageIO.read(in);
        }
    }
}