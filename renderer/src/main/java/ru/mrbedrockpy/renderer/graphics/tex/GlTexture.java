package ru.mrbedrockpy.renderer.graphics.tex;

import lombok.Getter;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL46C.*;
import static org.lwjgl.opengl.ARBBindlessTexture.*;

@Getter
public final class GlTexture implements AutoCloseable {
    private final int id;
    private long handle;
    private boolean resident;
    private final int width;
    private final int height;

    public GlTexture(int id, int w, int h) {
        this.id = id;
        this.width = w;
        this.height = h;

        if (org.lwjgl.opengl.GL.getCapabilities().GL_ARB_bindless_texture) {
            this.handle = glGetTextureHandleARB(id);
            glMakeTextureHandleResidentARB(this.handle);
            this.resident = true;
        }
    }

    public long handle() {
        if (org.lwjgl.opengl.GL.getCapabilities().GL_ARB_bindless_texture) {
            if (handle == 0L) handle = glGetTextureHandleARB(id);
            if (!resident) {
                glMakeTextureHandleResidentARB(handle);
                resident = true;
            }
        }
        return handle;
    }

    @Override
    public void close() {
        if (org.lwjgl.opengl.GL.getCapabilities().GL_ARB_bindless_texture) {
            if (resident && handle != 0L) {
                glMakeTextureHandleNonResidentARB(handle);
            }
        }
        glDeleteTextures(id);
    }
}