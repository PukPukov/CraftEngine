package ru.mrbedrockpy.renderer.graphics.tex;

public final class TextureRegion {
    public final int atlasIndex;     // какой атлас использовать в шейдере
    public final float u0, v0, u1, v1;

    public TextureRegion(int atlasIndex, float u0, float v0, float u1, float v1) {
        this.atlasIndex = atlasIndex;
        this.u0 = u0; this.v0 = v0; this.u1 = u1; this.v1 = v1;
    }
}