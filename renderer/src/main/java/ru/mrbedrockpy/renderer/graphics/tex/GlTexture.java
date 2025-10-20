package ru.mrbedrockpy.renderer.graphics.tex;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL46C.*;
import static org.lwjgl.opengl.ARBBindlessTexture.*;

public class GlTexture implements AutoCloseable {
    private final int id;
    private final long handle;
    private boolean resident;

    public GlTexture(int existingTexId) {
        this.id = existingTexId;
        long h = 0L;
        if (org.lwjgl.opengl.GL.getCapabilities().GL_ARB_bindless_texture) {
            h = glGetTextureHandleARB(id);
            glMakeTextureHandleResidentARB(h);
            resident = true;
        }
        this.handle = h;
    }
    public int id() { return id; }
    public long handle() { return handle; }
    public boolean isBindless() { return handle != 0L; }
    @Override public void close() {
        if (resident) {
            glMakeTextureHandleNonResidentARB(handle);
            resident = false;
        }
        // Не удаляем id, если он принадлежит внешнему атласу (FreeTextureAtlas сам управляет)
    }
}