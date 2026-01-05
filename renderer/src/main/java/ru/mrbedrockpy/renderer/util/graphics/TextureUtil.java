package ru.mrbedrockpy.renderer.util.graphics;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import ru.mrbedrockpy.renderer.graphics.Texture;
import ru.mrbedrockpy.renderer.util.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL46C.*;

public class TextureUtil {
    public static Texture fromBufferedImage(BufferedImage image) {
        ByteBuffer buf = ImageUtil.toByteBuffer(image);
        return new Texture(buf, image.getWidth(), image.getHeight());
    }

    public static BufferedImage toBufferedImage(Texture texture) {
        int width = texture.getWidth();
        int height = texture.getHeight();
        ByteBuffer buffer = texture.getBuffer();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[width * height];

        // предполагаем, что байты в порядке RGBA
        for (int i = 0; i < width * height; i++) {
            int r = buffer.get() & 0xFF;
            int g = buffer.get() & 0xFF;
            int b = buffer.get() & 0xFF;
            int a = buffer.get() & 0xFF;
            pixels[i] = ((a & 0xFF) << 24) |
                    ((r & 0xFF) << 16) |
                    ((g & 0xFF) << 8) |
                    (b & 0xFF);
        }

        image.setRGB(0, 0, width, height, pixels, 0, width);
        buffer.rewind();
        return image;
    }

    private static BufferedImage rotateCW(BufferedImage src) { // +90°
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage dst = new BufferedImage(h, w, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.translate(h, 0);
        g.rotate(Math.toRadians(90));
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return dst;
    }

    private static BufferedImage rotateCCW(BufferedImage src) { // -90°
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage dst = new BufferedImage(h, w, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.translate(0, w);
        g.rotate(Math.toRadians(-90));
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return dst;
    }

    private static BufferedImage rotate180(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.translate(w, h);
        g.rotate(Math.toRadians(180));
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return dst;
    }

    public static int loadCubemapFromAtlas3x2(String atlasPath) {
        BufferedImage atlas;
        try {
            atlas = ImageUtil.load(atlasPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load atlas: " + atlasPath, e);
        }

        int W = atlas.getWidth();
        int H = atlas.getHeight();

        int tile = Math.min(W / 3, H / 2);

        // 3x2, row-major: (0,0)=tile0, (1,0)=tile1, (2,0)=tile2, (0,1)=tile3, ...
        BufferedImage[] tiles = new BufferedImage[6];
        int idx = 0;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 3; col++) {
                int x = col * tile;
                int y = row * tile;
                tiles[idx++] = atlas.getSubimage(x, y, tile, tile);
            }
        }

        // Сопоставляем тайлы с OpenGL-гранями: +X, -X, +Y, -Y, +Z, -Z
        // Ниже предполагается наиболее распространённая раскладка атласа:
        // [0]=right, [1]=left, [2]=top, [3]=bottom, [4]=front, [5]=back
        BufferedImage[] faces = new BufferedImage[6];
        faces[0] = tiles[5]; // +X right
        faces[1] = tiles[3]; // -X left
        faces[2] = tiles[1]; // +Y top
        faces[3] = tiles[0]; // -Y bottom
        faces[4] = tiles[4]; // +Z front
        faces[5] = tiles[2];


        // Поправки ориентации под Z-up (результат наших прошлых правок):
        for (int i = 0; i < 6; i++) {
            BufferedImage img = faces[i];
            img = switch (i) {
                case 0 -> // +X right — был перевёрнут → 180°
                        rotateCCW(img);
                case 1 -> // -X left — ок
                        flipHorizontal(rotateCW(img));
                case 2 -> // +Y top — ок
                        rotateCCW(img);
                case 3 -> // -Y bottom — ок
                        flipHorizontal(rotateCW(img));
                case 4 -> // +Z front — нужно +90°
                        flipHorizontal(img);
                case 5 -> // -Z back — нужно +90°
                        rotate180(img);
                default -> img;
            };
            faces[i] = img;
        }

        // Загрузка в cubemap
        int texId = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, texId);
        for (int i = 0; i < 6; i++) {
            ByteBuffer buffer = ImageUtil.toByteBuffer(faces[i]); // твой существующий метод
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGBA8,
                    faces[i].getWidth(), faces[i].getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        }
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        return texId;
    }

    private static BufferedImage flipHorizontal(BufferedImage src) {
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        // (dst x0,y0,x1,y1, src x1,y0,x0,y1) — зеркалим по X
        g.drawImage(src, 0, 0, w, h, w, 0, 0, h, null);
        g.dispose();
        return dst;
    }
}
