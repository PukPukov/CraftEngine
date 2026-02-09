package ru.mrbedrockpy.craftengine.render.graphics.tex;

import ru.mrbedrockpy.craftengine.util.id.RL;

public interface UvProvider {
    float[] getNormalizedUvs(RL key);
}