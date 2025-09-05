package ru.mrbedrockpy.renderer.graphics;

import lombok.AccessLevel;
import lombok.Getter;
import org.joml.Vector2i;
import ru.mrbedrockpy.renderer.api.IBlock;
import ru.mrbedrockpy.renderer.util.ImageUtil;
import ru.mrbedrockpy.renderer.util.graphics.TextureUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL46C.*;

public class TextureAtlas {
    private final int tileSize = 32;
    @Getter(AccessLevel.PUBLIC)
    private final int atlasSize;
    private final Map<String, Rectangle> uvMap = new HashMap<>();
    private int currentX = 0;
    private int currentY = 0;

    private final BufferedImage atlasImage;

    public TextureAtlas(int atlasSize) {
        this.atlasSize = atlasSize;
        this.atlasImage = new BufferedImage(tileSize * atlasSize, tileSize * atlasSize, BufferedImage.TYPE_INT_ARGB);

    }

    public void addTile(String name, BufferedImage tile) {
        if (currentY >= atlasSize) {
            throw new RuntimeException("TextureAtlas overflow");
        }

        Graphics g = atlasImage.getGraphics();
        g.drawImage(tile, currentX * tileSize, currentY * tileSize, null);
        g.dispose();

        uvMap.put(name, new Rectangle(currentX * tileSize, currentY * tileSize, tile.getTileWidth(), tile.getTileHeight()));

        currentX++;
        if (currentX >= atlasSize) {
            currentX = 0;
            currentY++;
        }
    }

    public Texture buildAtlas() {
        return TextureUtil.fromBufferedImage(atlasImage);
    }

    public Rectangle uv(String name) {
        return uvMap.get(name);
    }


    public float[] normalizedUv(String name) {
        Rectangle r = uv(name);
        float atlasPix = tileSize * atlasSize;
        float x0 = r.x, x1 = r.x + r.width;
        float y0 = r.y, y1 = r.y + r.height;
        float u0 = x0 / atlasPix, u1 = x1 / atlasPix;
        float v0 = y1 / atlasPix, v1 = y0 / atlasPix;
        return new float[]{u0,v0, u1,v0, u1,v1, u0,v1};
    }
}