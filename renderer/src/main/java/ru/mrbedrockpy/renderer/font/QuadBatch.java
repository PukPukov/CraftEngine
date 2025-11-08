package ru.mrbedrockpy.renderer.font;

public interface QuadBatch {
    void maskedQuad(float x0, float y0, float x1, float y1,
                    float u0, float v0, float u1, float v1,
                    int rgba, float italicSkew);
    void texturedQuad(float x0, float y0, float x1, float y1,
                      float u0, float v0, float u1, float v1);

    void solidQuad(float x0, float y0, float x1, float y1, int rgba);
}