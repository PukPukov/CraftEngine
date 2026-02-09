package ru.mrbedrockpy.craftengine.render.window;

import org.joml.Matrix4f;

public final class ScaleManager {
    private int fbWidth = 1, fbHeight = 1;
    private int uiScale = 5;

    public void setFramebufferSize(int fbW, int fbH) {
        this.fbWidth  = Math.max(1, fbW);
        this.fbHeight = Math.max(1, fbH);
    }

    public int logicalWidth()  { return toLogicalX(fbWidth); }
    public int logicalHeight() { return toLogicalY(fbHeight); }

    public int toLogicalX(int physicalX) { return Math.ceilDiv(physicalX, uiScale); }
    public int toLogicalY(int physicalY) { return Math.ceilDiv(physicalY, uiScale); }

    public Matrix4f ortho() {
        return new Matrix4f().ortho(
                0f, logicalWidth(),
                logicalHeight(), 0f,
                -1f, 1f
        );
    }

    public float coofX() {
        return (float) logicalWidth()/ fbWidth;
    }

    public float coofY() {
        return (float) logicalHeight() / fbHeight;
    }
}