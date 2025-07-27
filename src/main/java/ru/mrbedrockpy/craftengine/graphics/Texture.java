package ru.mrbedrockpy.craftengine.graphics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL46C.*;

@Getter
@AllArgsConstructor
public class Texture {
    private static final Map<String, Texture> cache = new HashMap<>();
    private final int id;

    private final int width;
    private final int height;

    public static Texture load(String filename) {
        if (cache.containsKey(filename)) {
            return cache.get(filename);
        }
        STBImage.stbi_set_flip_vertically_on_load(true);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer widthBuf = stack.mallocInt(1);
            IntBuffer heightBuf = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);
            ByteBuffer imageBuffer;
            try(InputStream inputStream = Texture.class.getResourceAsStream("/" + filename)) {
                if (inputStream == null) throw new RuntimeException("Resource not found: " + filename);
                byte[] imageData = inputStream.readAllBytes();
                ByteBuffer buffer = ByteBuffer.allocateDirect(imageData.length).put(imageData).flip();
                imageBuffer = STBImage.stbi_load_from_memory(buffer, widthBuf, heightBuf, channels, 4);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (imageBuffer == null) throw new RuntimeException("Image load failed: " + STBImage.stbi_failure_reason());
            int width = widthBuf.get(0);
            int height = heightBuf.get(0);
            int textureId = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureId);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer);
            glGenerateMipmap(GL_TEXTURE_2D);
            STBImage.stbi_image_free(imageBuffer);
            glBindTexture(GL_TEXTURE_2D, 0);
            return new Texture(textureId, width, height);
        }
    }

    public static Texture fromBufferedImage(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int[] pixels = new int[width * height];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        for (int y = height - 1; y >= 0; y--) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }
        buffer.flip();

        int textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0);

        return new Texture(textureId, width, height);
    }

    public void use() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void dispose() {
        glDeleteTextures(id);
    }
}
