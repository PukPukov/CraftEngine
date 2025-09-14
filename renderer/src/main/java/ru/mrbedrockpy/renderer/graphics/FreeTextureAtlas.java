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
    @Getter
    private int widthPx, heightPx;              // больше не final — растём динамически
    private BufferedImage atlasImage;
    private final int glTexId;
    @Getter
    private final Map<String, Rectangle> uvMap = new HashMap<>();
    private final List<Shelf> shelves = new ArrayList<>();
    private int usedHeight = 0;

    private static class Shelf {
        int y;
        int height;
        int x;
        Shelf(int y, int height) { this.y = y; this.height = height; this.x = 0; }
    }

    public FreeTextureAtlas() { this(512, 512); }
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
    }

    /** Биндит атлас в активный текстурный юнит */
    public void use() { glBindTexture(GL_TEXTURE_2D, glTexId); }

    /** Проверяет, есть ли уже тайл в атласе */
    public boolean contains(String name) { return uvMap.containsKey(name); }

    /**
     * Добавляет тайл. Если не влезает — атлас увеличится ровно настолько, насколько нужно.
     * Возвращает прямоугольник размещения в пикселях.
     */
    public Rectangle addTexture(String name, BufferedImage tile) {
        if (uvMap.containsKey(name)) return uvMap.get(name);

        int w = tile.getWidth();
        int h = tile.getHeight();
        if (w <= 0 || h <= 0) throw new IllegalArgumentException("Tile has invalid size: " + w + "x" + h);

        // 1) Попробуем найти полку, куда поместится (по высоте и ширине)
        Shelf shelf = findShelfToFit(w, h);

        // 2) Если не нашли — обеспечим вместимость (минимально возможным ростом) и повторим поиск
        if (shelf == null) {
            ensureCapacityFor(w, h);
            shelf = findShelfToFit(w, h);
            if (shelf == null) {
                // на всякий случай, но по логике ensureCapacityFor() мы обязаны были создать возможность
                throw new IllegalStateException("Failed to fit texture after resize");
            }
        }

        // 3) Рисуем в RAM-изображение
        int x0 = shelf.x;
        int y0 = shelf.y;
        Graphics g = atlasImage.getGraphics();
        g.drawImage(tile, x0, y0, null);
        g.dispose();

        // 4) Заливаем в GPU (частично)
        ByteBuffer data = ImageUtil.toByteBuffer(tile);
        use();
        glTexSubImage2D(GL_TEXTURE_2D, 0, x0, y0, w, h, GL_RGBA, GL_UNSIGNED_BYTE, data);

        // 5) Обновляем метаданные
        Rectangle rect = new Rectangle(x0, y0, w, h);
        uvMap.put(name, rect);
        shelf.x += w;

        return rect;
    }

    /** Возвращает UV-координаты: [u0,v1, u1,v1, u1,v0, u0,v0] (нижний левый = (0,0)). */
    public float[] getNormalizedUvs(String name) {
        Rectangle r = uvMap.get(name);
        if (r == null) throw new IllegalArgumentException("Нет тайла: " + name);

        float u0 = r.x         / (float) widthPx;
        float v1 = r.y         / (float) heightPx;
        float u1 = (r.x+r.width)  / (float) widthPx;
        float v0 = (r.y+r.height) / (float) heightPx;

        return new float[]{u0, v1,  u1, v1,  u1, v0,  u0, v0};
    }

    /** Сохраняет атлас в PNG-файл для отладки */
    public void saveAsPng(String path) throws IOException {
        File out = new File(Paths.get(path).toUri());
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
        use();
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
