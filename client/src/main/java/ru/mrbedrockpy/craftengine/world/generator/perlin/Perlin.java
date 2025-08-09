package ru.mrbedrockpy.craftengine.world.generator.perlin;

public final class Perlin {
    private final int[] p = new int[512];

    public Perlin(long seed) {
        int[] perm = new int[256];
        for (int i = 0; i < 256; i++) perm[i] = i;
        java.util.Random r = new java.util.Random(seed);
        for (int i = 255; i > 0; i--) {
            int j = r.nextInt(i + 1);
            int t = perm[i]; perm[i] = perm[j]; perm[j] = t;
        }
        for (int i = 0; i < 512; i++) p[i] = perm[i & 255];
    }

    public float noise(float x, float y) {
        int xi = fastFloor(x) & 255;
        int yi = fastFloor(y) & 255;
        float xf = x - fastFloor(x);
        float yf = y - fastFloor(y);

        float u = fade(xf);
        float v = fade(yf);

        int aa = p[p[xi] + yi];
        int ab = p[p[xi] + yi + 1];
        int ba = p[p[xi + 1] + yi];
        int bb = p[p[xi + 1] + yi + 1];

        float x1 = lerp(grad(aa, xf,   yf),   grad(ba, xf-1, yf),   u);
        float x2 = lerp(grad(ab, xf, yf-1),   grad(bb, xf-1, yf-1), u);
        return lerp(x1, x2, v);
    }

    private static int fastFloor(float x) { int i = (int)x; return x < i ? i - 1 : i; }
    private static float fade(float t) { return t*t*t*(t*(t*6 - 15) + 10); }
    private static float lerp(float a, float b, float t) { return a + t*(b - a); }

    private static float grad(int h, float x, float y) {
        return switch (h & 3) {
            case 0 -> x + y;
            case 1 -> -x + y;
            case 2 -> x - y;
            default -> -x - y;
        };
    }
}