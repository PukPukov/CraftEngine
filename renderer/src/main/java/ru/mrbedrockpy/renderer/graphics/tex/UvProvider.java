package ru.mrbedrockpy.renderer.graphics.tex;

import ru.mrbedrockpy.craftengine.core.util.id.RL;

public interface UvProvider {
    float[] getNormalizedUvs(RL key);
}