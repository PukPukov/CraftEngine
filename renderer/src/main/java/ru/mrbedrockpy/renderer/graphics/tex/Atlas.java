package ru.mrbedrockpy.renderer.graphics.tex;

import lombok.Getter;
import lombok.Setter;
import ru.mrbedrockpy.craftengine.core.util.id.RL;
import ru.mrbedrockpy.renderer.graphics.Texture;

public class Atlas implements UvProvider {
    private final RL name;
    protected Texture texture;
    @Setter
    private UvProvider uv;

    public Atlas(RL name) {
        this.name = name;
        this.uv = this;
    }

    public RL name() { return name; }
    public Texture texture() { return texture; }

    public TextureRegion region(int atlasIndex, RL key) {
        float[] a = uv.getNormalizedUvs(key);
        if (a == null) return null;

        // Порядок из getNormalizedUvs: 0:u0,1:v0, 2:u1,3:v0, 4:u1,5:v1, 6:u0,7:v1
        float u0 = a[0], v0 = a[1];
        float u1 = a[4], v1 = a[5];

        int atlasW = texture.getWidth();
        int atlasH = texture.getHeight();

        int regionW = Math.max(1, Math.round((u1 - u0) * atlasW));
        int regionH = Math.max(1, Math.round((v1 - v0) * atlasH));

        return new TextureRegion(atlasIndex, u0, v0, u1, v1, regionW, regionH);
    }

    @Override
    public float[] getNormalizedUvs(RL key) {
        return new float[0];
    }
}