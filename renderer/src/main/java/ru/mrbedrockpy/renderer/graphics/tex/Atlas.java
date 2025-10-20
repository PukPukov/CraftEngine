package ru.mrbedrockpy.renderer.graphics.tex;

public final class Atlas {
    private final String name;
    private final GlTexture texture;
    private final UvProvider uv;

    public Atlas(String name, GlTexture texture, UvProvider uv) {
        this.name = name;
        this.texture = texture;
        this.uv = uv;
    }
    public String name() { return name; }
    public GlTexture texture() { return texture; }

    public TextureRegion region(int atlasIndex, String key) {
        float[] a = uv.getNormalizedUvs(key);
        if (a == null) return null;
        return new TextureRegion(atlasIndex, a[0], a[1], a[2], a[3]);
    }
}