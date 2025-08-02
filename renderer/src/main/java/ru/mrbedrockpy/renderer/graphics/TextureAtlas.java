package ru.mrbedrockpy.renderer.graphics;

import lombok.AccessLevel;
import lombok.Getter;
import org.joml.Vector2i;
import ru.mrbedrockpy.renderer.api.IBlock;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class TextureAtlas {
    private final int tileSize;
    @Getter(AccessLevel.PUBLIC)
    private final int atlasSize;
    private final Map<String, Vector2i> uvMap = new HashMap<>();
    private int currentX = 0;
    private int currentY = 0;
    
    private final BufferedImage atlasImage;
    
    public TextureAtlas(int tileSize, int atlasSize) {
        this.tileSize = tileSize;
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
        
        uvMap.put(name, new Vector2i(currentX, currentY));
        
        currentX++;
        if (currentX >= atlasSize) {
            currentX = 0;
            currentY++;
        }
    }
    
    public BufferedImage buildAtlas() {
        return atlasImage;
    }
    
    public Vector2i uv(String name) {
        return uvMap.get(name);
    }

    public float[] normalizedUV(String name, IBlock.Direction direction) {
        Vector2i uv = uv(name);
        float tileCount = atlasSize;
        float unit = 1.0f / tileCount;
        
        float baseX = uv.x * unit;
        float baseY = 1.0f - (uv.y + 1) * unit;
        
        float[] faceUV = FaceData.FACE_UVS[direction.ordinal()];
        float[] result = new float[12];
        
        for (int i = 0; i < 6; i++) {
            float u = faceUV[i * 2];
            float v = faceUV[i * 2 + 1];
            result[i * 2]     = baseX + u * unit;
            result[i * 2 + 1] = baseY + v * unit;
        }
        
        return result;
    }
}