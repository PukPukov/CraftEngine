package ru.mrbedrockpy.renderer.graphics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.ARBBindlessTexture.*;
import static org.lwjgl.opengl.GL46C.*;

@Getter
public class Texture implements AutoCloseable {
    private final int id;
    private final ByteBuffer buffer;
    private final int width;
    private final int height;

    // 🆕 Bindless handle
    private final long handle;
    private boolean resident;

    public Texture(ByteBuffer buffer, int width, int height) {
        this.buffer = buffer;
        this.width = width;
        this.height = height;

        id = glGenTextures();
        initGlParams();

        handle = glGetTextureHandleARB(id);
        glMakeTextureHandleResidentARB(handle);
        resident = true;
    }

    public void use() {
        // ❌ больше не нужно в bindless-шейдерах
        // glBindTexture(GL_TEXTURE_2D, id);
    }

    public void unbind() {
        // ❌ тоже не нужно
        // glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void initGlParams() {
        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glGenerateMipmap(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    @Override
    public void close() {
        if (resident) {
            glMakeTextureHandleNonResidentARB(handle);
            resident = false;
        }
        glDeleteTextures(id);
    }
}