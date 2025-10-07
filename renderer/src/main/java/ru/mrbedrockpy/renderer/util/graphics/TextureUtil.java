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

public class TextureUtil{
    public static Texture fromBufferedImage(BufferedImage image) {
        ByteBuffer buf = ImageUtil.toByteBuffer(image);
        return new Texture(buf, image.getWidth(), image.getHeight());
    }

    public static int loadCubemap(String[] faces) {
        int texId = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, texId);

        for (int i = 0; i < faces.length; i++) {
            BufferedImage img;
            try {
                img = ImageUtil.load(faces[i]); // твой загрузчик
            } catch (IOException e) {
                throw new RuntimeException("Failed to load cubemap face: " + faces[i], e);
            }

            // гарантируем совместимый формат для операций
//            img = toARGB(img);

            img = switch (i) {
                case 0 -> // +X → right
                        rotateCCW(img); // +90°
                case 2 -> // +Y → top
                        rotateCW(rotateCW(img)); // норм
                case 3 -> // -Y → bottom
                        img; // норм
                case 4 -> // +Z → front
                        rotateCW(img); // +90°
                case 5 -> // -Z → back
                        rotateCW(img); // +90°
                default -> // left (-X)
                        rotateCW(img); // норм
            };


            ByteBuffer buffer = ImageUtil.toByteBuffer(img); // твоя функция
            glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGBA8,
                    img.getWidth(), img.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        return texId;
    }

    // ---------- вспомогалки (можно положить в ImageUtil, если хочешь) ----------
    private static BufferedImage toARGB(BufferedImage src) {
        if (src.getType() == BufferedImage.TYPE_INT_ARGB) return src;
        BufferedImage dst = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return dst;
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
