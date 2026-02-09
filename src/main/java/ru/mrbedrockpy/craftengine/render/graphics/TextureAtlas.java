package ru.mrbedrockpy.craftengine.render.graphics;

import lombok.AccessLevel;
import lombok.Getter;
import ru.mrbedrockpy.craftengine.render.graphics.tex.Atlas;
import ru.mrbedrockpy.craftengine.render.util.graphics.TextureUtil;
import ru.mrbedrockpy.craftengine.util.id.RL;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import static org.lwjgl.opengl.GL46C.*;

public class TextureAtlas extends Atlas implements AutoCloseable {
    private final int tileSize = 32;
    @Getter(AccessLevel.PUBLIC)
    private final int atlasSize;
    private final Map<RL, Rectangle> uvMap = new HashMap<>();
    private int currentX = 0;
    private int currentY = 0;

    private final BufferedImage atlasImage;

    public TextureAtlas(int atlasSize) {
        super(RL.of("blocks"));
        this.atlasSize = atlasSize;
        this.atlasImage = new BufferedImage(tileSize * atlasSize, tileSize * atlasSize, BufferedImage.TYPE_INT_ARGB);
        this.texture = new Texture(glGenTextures(), tileSize * atlasSize, tileSize * atlasSize);
    }

    public void addTile(RL name, BufferedImage tile) {
        if (currentY >= atlasSize) throw new RuntimeException("TextureAtlas overflow");

        int w = tile.getWidth();
        int h = tile.getHeight();

        int px = currentX * tileSize;
        int py = currentY * tileSize;

        Graphics2D g = atlasImage.createGraphics();
        try {
            g.setComposite(AlphaComposite.Src);
            g.drawImage(tile, px, py, null);
        } finally {
            g.dispose();
        }

        uvMap.put(name, new Rectangle(px, py, w, h));

        currentX++;
        if (currentX >= atlasSize) { currentX = 0; currentY++; }
    }

    public Texture buildAtlas() {
        this.texture.close();
        this.texture = TextureUtil.fromBufferedImage(atlasImage);
        return texture;
    }

    public Rectangle uv(RL name) {
        return uvMap.get(name);
    }


    public float[] getNormalizedUvs(RL name) {
        Rectangle r = uv(name);
        try {
            if(r == null){
                throw new IllegalArgumentException("Rectangle for: \'" + name + "\' is null");
            }
        } catch (Exception e){
            e.printStackTrace();
            return new float[]{};
        }
        float atlasPix = tileSize * atlasSize;
        float x0 = r.x, x1 = r.x + r.width;
        float y0 = r.y, y1 = r.y + r.height;
        float u0 = x0 / atlasPix, u1 = x1 / atlasPix;
        float v0 = y1 / atlasPix, v1 = y0 / atlasPix;
        return new float[]{u0,v0, u1,v0, u1,v1, u0,v1};
    }

    @Override
    public void close() throws Exception {
        uvMap.clear();
    }
}