package ru.mrbedrockpy.renderer.font;

public class Glyph {
    public final float u0, v0, u1, v1;
    public final float width, height;
    public final float offsetX, offsetY;
    public final float advanceX;

    public Glyph(float u0, float v0, float u1, float v1,
                 float width, float height,
                 float offsetX, float offsetY, float advanceX) {
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
        this.width = width;
        this.height = height;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.advanceX = advanceX;
    }
}
