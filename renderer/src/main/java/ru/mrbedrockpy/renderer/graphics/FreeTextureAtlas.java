package ru.mrbedrockpy.renderer.graphics;

import lombok.Getter;
import ru.mrbedrockpy.renderer.util.ImageUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import static org.lwjgl.opengl.GL46C.*;

public class FreeTextureAtlas {
    private final int widthPx, heightPx;
    private final BufferedImage atlasImage;
    private final int glTexId;
    @Getter
    private final Map<String, Rectangle> uvMap = new HashMap<>();
    private final List<Shelf> shelves = new ArrayList<>();
    private int usedHeight = 0;

    public int getGlTexId() {
        return glTexId;
    }

    /** Полка внутри атласа */
    private static class Shelf {
        int y;
        int height;
        int x;

        Shelf(int y, int height) {
            this.y = y;
            this.height = height;
            this.x = 0;
        }
    }

    /**
     * @param widthPx  ширина атласа в пикселях
     * @param heightPx высота атласа в пикселях
     */
    public FreeTextureAtlas(int widthPx, int heightPx) {
        this.widthPx  = widthPx;
        this.heightPx = heightPx;
        this.atlasImage = new BufferedImage(widthPx, heightPx, BufferedImage.TYPE_INT_ARGB);

        glTexId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, glTexId);
        ByteBuffer empty = ImageUtil.toByteBuffer(atlasImage);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, widthPx, heightPx, 0, GL_RGBA, GL_UNSIGNED_BYTE, empty);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    }

    /** Биндит атлас в активный текстурный юнит */
    public void bind() {
        glBindTexture(GL_TEXTURE_2D, glTexId);
    }

    /** Проверяет, есть ли уже тайл в атласе */
    public boolean contains(String name) {
        return uvMap.containsKey(name);
    }

    /**
     * Добавляет тайл с автоматическим выбором места через shelf-packing.
     * @param name ключ тайла
     * @param tile изображение
     */
    public void addTexture(String name, BufferedImage tile) {
        int w = tile.getWidth(), h = tile.getHeight();

        Shelf chosen = null;
        for (Shelf s : shelves) {
            if (h <= s.height && s.x + w <= widthPx) {
                chosen = s;
                break;
            }
        }

        if (chosen == null) {
            if (usedHeight + h > heightPx) {
                throw new RuntimeException("TextureAtlas overflow: нельзя разместить тайл высотой " + h);
            }
            chosen = new Shelf(usedHeight, h);
            shelves.add(chosen);
            usedHeight += h;
        }

        int x0 = chosen.x;
        int y0 = chosen.y;

        Graphics g = atlasImage.getGraphics();
        g.drawImage(tile, x0, y0, null);
        g.dispose();

        uvMap.put(name, new Rectangle(x0, y0, w, h));

        ByteBuffer data = ImageUtil.toByteBuffer(tile);
        this.bind();
        glTexSubImage2D(GL_TEXTURE_2D, 0, x0, y0, w, h, GL_RGBA, GL_UNSIGNED_BYTE, data);

        chosen.x += w;
    }

    /**
     * Возвращает UV-координаты для шейдера: [u0,v0, u1,v0, u1,v1, u0,v1]
     * Нижний левый угол = (0,0).
     */
    public float[] getNormalizedUvs(String name) {
        Rectangle r = uvMap.get(name);
        if (r == null) throw new IllegalArgumentException("Нет тайла: " + name);

        float u0 = r.x        / (float)widthPx;
        float v1 = r.y        / (float)heightPx;
        float u1 = u0 + r.width / (float) widthPx;
        float v0 = v1 + r.height / (float) heightPx;


        return new float[]{u0, v1,
                           u1, v1,
                           u1, v0,
                           u0, v0};
    }

    /** Сохраняет атлас в PNG-файл для отладки */
    public void saveAsPng(String path) throws IOException {
        File out = new File(Paths.get(path).toUri());
        out.getParentFile().mkdirs();
        ImageIO.write(atlasImage, "PNG", out);
    }
}