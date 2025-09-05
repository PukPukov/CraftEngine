package ru.mrbedrockpy.renderer.util.graphics;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import ru.mrbedrockpy.renderer.graphics.Texture;
import ru.mrbedrockpy.renderer.util.ImageUtil;

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
}
