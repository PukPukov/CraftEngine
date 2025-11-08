package ru.mrbedrockpy.renderer.graphics;

import lombok.Getter;
import ru.mrbedrockpy.craftengine.core.util.id.RL;
import ru.mrbedrockpy.renderer.graphics.tex.UvProvider;
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

public class FreeTextureAtlas implements UvProvider {
    @Getter
    private int widthPx, heightPx;
    private BufferedImage atlasImage;
    private final int glTexId;
    @Getter
    private final Map<RL, Rectangle> uvMap = new HashMap<>();
    private final List<Shelf> shelves = new ArrayList<>();
    private int usedHeight = 0;

    public int getTextureId() {
        return glTexId;
    }

    private static class Shelf {
        int y;
        int height;
        int x;
        Shelf(int y, int height) { this.y = y; this.height = height; this.x = 0; }
    }

    public FreeTextureAtlas() { this(4096, 4096); }
    public FreeTextureAtlas(int widthPx, int heightPx) {
        this.widthPx  = Math.max(1, widthPx);
        this.heightPx = Math.max(1, heightPx);

        this.atlasImage = new BufferedImage(this.widthPx, this.heightPx, BufferedImage.TYPE_INT_ARGB);

        glTexId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, glTexId);
        ByteBuffer empty = ImageUtil.toByteBuffer(atlasImage);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, this.widthPx, this.heightPx, 0, GL_RGBA, GL_UNSIGNED_BYTE, empty);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }

    public boolean contains(RL name) { return uvMap.containsKey(name); }

    public void addTexture(RL name, BufferedImage tile) {
        RL key = normalize(name);
        Rectangle cached = uvMap.get(key);
        if (cached != null) return;

        int w = tile.getWidth();
        int h = tile.getHeight();
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException("Tile has invalid size: " + w + "x" + h + " for " + key);
        }

        // 1) Находим «полку» (shelf), куда поместится
        Shelf shelf = findShelfToFit(w, h);

        // 2) Если не нашли — растим атлас и повторяем
        if (shelf == null) {
            ensureCapacityFor(w, h);
            shelf = findShelfToFit(w, h);
            if (shelf == null) {
                throw new IllegalStateException("Failed to fit texture after resize for " + key);
            }
        }

        // 3) Рисуем в RAM-изображение
        final int x0 = shelf.x;
        final int y0 = shelf.y;

        Graphics2D g = atlasImage.createGraphics();
        try {
            g.setComposite(AlphaComposite.Src);     // без лишнего бленда
            g.drawImage(tile, x0, y0, null);
        } finally {
            g.dispose();
        }

        // 4) Заливаем изменившийся блок в GPU
        ByteBuffer data = ImageUtil.toByteBuffer(tile); // RGBA 8bit
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);          // на случай «не-кратно-4» ширины
        glTexSubImage2D(GL_TEXTURE_2D, 0, x0, y0, w, h, GL_RGBA, GL_UNSIGNED_BYTE, data);

        // 5) Обновляем метаданные
        Rectangle rect = new Rectangle(x0, y0, w, h);
        uvMap.put(key, rect);
        shelf.x += w;
    }

    private static RL normalize(RL id) {
        String ns = id.namespace();
        String p  = id.path().replace('\\', '/');

        if (p.startsWith("textures/")) p = p.substring("textures/".length());
        if (p.endsWith(".png")) p = p.substring(0, p.length() - 4);

        return RL.of(ns, p.toLowerCase(Locale.ROOT));
    }


    /** Возвращает UV-координаты: [u0,v1, u1,v1, u1,v0, u0,v0] (нижний левый = (0,0)). */
    public float[] getNormalizedUvs(RL name) {
        Rectangle r = uvMap.get(name);
        if (r == null) {
            System.err.println("Texture not found: " + name);
            return new float[8];
        }

        float u0 = r.x         / (float) widthPx;
        float v1 = r.y         / (float) heightPx;
        float u1 = (r.x+r.width)  / (float) widthPx;
        float v0 = (r.y+r.height) / (float) heightPx;

        return new float[]{u0, v1,  u1, v1,  u1, v0,  u0, v0};
    }

    /** Сохраняет атлас в PNG-файл для отладки */
    public void saveAsPng(RL path) throws IOException {
        File out = new File(Paths.get(path.toString()).toUri());
        File parent = out.getParentFile();
        if (parent != null) parent.mkdirs();
        ImageIO.write(atlasImage, "PNG", out);
    }

    // =========================
    // ВНУТРЕННЯЯ ЛОГИКА РАЗМЕЩЕНИЯ/РОСТА
    // =========================

    /** Ищет полку, куда поместится прямоугольник w×h. */
    private Shelf findShelfToFit(int w, int h) {
        Shelf best = null;
        for (Shelf s : shelves) {
            if (h <= s.height && s.x + w <= widthPx) {
                best = s; // первый подходящий (можно улучшить критерий — min остатка)
                break;
            }
        }
        // Нет подходящей — попробуем создать новую полку, если хватает высоты
        if (best == null && usedHeight + h <= heightPx) {
            Shelf s = new Shelf(usedHeight, h);
            shelves.add(s);
            usedHeight += h;
            best = s;
        }
        return best;
    }

    /** Обеспечивает возможность разместить прямоугольник w×h: минимально увеличивает ширину/высоту и пересоздаёт текстуру. */
    private void ensureCapacityFor(int w, int h) {
        // Случай А: есть полки достаточной высоты, но не хватает ШИРИНЫ -> увеличиваем width столько, чтобы поместилось
        int minWidthNeeded = widthPx;
        boolean hasHeightShelf = false;
        for (Shelf s : shelves) {
            if (h <= s.height) {
                hasHeightShelf = true;
                if (s.x + w > minWidthNeeded) minWidthNeeded = s.x + w;
            }
        }

        int newWidth = widthPx;
        int newHeight = heightPx;

        if (hasHeightShelf && minWidthNeeded > widthPx) {
            newWidth = nextPow2(minWidthNeeded); // минимально достаточная степень двойки
        }

        // Случай B: нет полки достаточной высоты -> нужен новый shelf
        // если по высоте не влезает — увеличиваем height
        if (!hasHeightShelf) {
            int minHeightNeeded = usedHeight + h;
            if (minHeightNeeded > heightPx) {
                newHeight = nextPow2(minHeightNeeded);
            }
        } else {
            // даже если полка по высоте есть, но свободной высоты нет (для создания новой) — тоже увеличим
            if (usedHeight + h > heightPx) {
                int minHeightNeeded = usedHeight + h;
                newHeight = nextPow2(minHeightNeeded);
            }
        }

        // Если ничего не поменялось — попробуем хотя бы удвоить одну из сторон,
        // чтобы избежать зацикливания (редкий случай с экзотическими входами)
        if (newWidth == widthPx && newHeight == heightPx) {
            // Выберем сторону роста с меньшим приростом площади
            long areaW = (long) nextPow2(widthPx * 2) * heightPx;
            long areaH = (long) widthPx * nextPow2(heightPx * 2);
            if (areaW <= areaH) newWidth = nextPow2(widthPx * 2);
            else newHeight = nextPow2(heightPx * 2);
        }

        // Если размеры изменились — реально делаем ресайз
        if (newWidth != widthPx || newHeight != heightPx) {
            resizeAtlas(newWidth, newHeight);
        }
    }

    /** Пересоздаёт RAM-изображение и GL-текстуру большего размера, копируя старое содержимое. */
    private void resizeAtlas(int newW, int newH) {
        // 1) RAM: новый BufferedImage и копия старого
        BufferedImage newImg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImg.createGraphics();
        g2.setComposite(AlphaComposite.Src);
        g2.drawImage(atlasImage, 0, 0, null);
        g2.dispose();
        atlasImage = newImg;

        // 2) GPU: переинициализируем текстуру и зальём весь снимок
        ByteBuffer all = ImageUtil.toByteBuffer(atlasImage);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, newW, newH, 0, GL_RGBA, GL_UNSIGNED_BYTE, all);
        // фильтры уже выставлены в конструкторе; повторять не обязательно

        // 3) Обновить поля
        widthPx = newW;
        heightPx = newH;
        // polки остаются валидны: увеличилась ширина/высота контейнера, их y/height/x не менялись
        // (x — использованная ширина полки — также валидна)
    }

    private static int nextPow2(int v) {
        if (v <= 1) return 1;
        v--;
        v |= v >> 1;
        v |= v >> 2;
        v |= v >> 4;
        v |= v >> 8;
        v |= v >> 16;
        return v + 1;
    }
}
