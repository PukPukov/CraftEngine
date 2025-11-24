package ru.mrbedrockpy.renderer.graphics.tex;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public final class TextureRegion {
    public final int atlasIndex;
    public final float u0, v0, u1, v1;
    public final int texW, texH;
}