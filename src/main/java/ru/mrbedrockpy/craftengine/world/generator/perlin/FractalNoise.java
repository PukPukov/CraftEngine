package ru.mrbedrockpy.craftengine.world.generator.perlin;

public final class FractalNoise {
    private final Perlin base;
    public float scale = 0.01f;
    public int octaves = 4;
    public float lacunarity = 2.0f;
    public float gain = 0.5f;

    public FractalNoise(long seed) { this.base = new Perlin(seed); }

    public float fbm(float x, float y) {
        float sum = 0f;
        float amp = 1f;
        float freq = scale;
        float norm = 0f;

        for (int i = 0; i < octaves; i++) {
            sum += base.noise(x * freq, y * freq) * amp;
            norm += amp;
            amp *= gain;
            freq *= lacunarity;
        }
        return sum / Math.max(1e-6f, norm);
    }
}