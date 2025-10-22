package ru.mrbedrockpy.renderer.window;

import lombok.Getter;
import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import ru.mrbedrockpy.craftengine.core.util.config.CraftEngineConfig;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL46C.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

public class Window {

    @Getter private static long window;
    @Getter private static int width, height;
    @Getter private static String title;
    @Getter private static boolean vsync;
    @Getter private static boolean fullscreen;

    private static int windowedX, windowedY, windowedW, windowedH;

    public static void initialize(WindowSettings settings) {
        Window.width  = settings.getWidth();
        Window.height = settings.getHeight();
        Window.title  = settings.getTitle();
        Window.vsync  = settings.isVsync();
        Window.fullscreen = false;

        if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            glfwInitHint(GLFW_PLATFORM, GLFW_PLATFORM_X11);
        }

        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);

        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL) throw new IllegalStateException("Failed to create the GLFW window");

        centerOnPrimaryMonitor();

        glfwMakeContextCurrent(window);
        setVsync(vsync);
        GL.createCapabilities();

        glfwSetFramebufferSizeCallback(window, (win, w, h) -> {
            if (w <= 0 || h <= 0) return;
            glViewport(0, 0, w, h);
        });
        glfwSetWindowSizeCallback(window, (win, w, h) -> {
            if (!fullscreen) { windowedW = w; windowedH = h; }
            Window.width = w;
            Window.height = h;
        });
        glfwSetWindowPosCallback(window, (win, x, y) -> {
            if (!fullscreen) { windowedX = x; windowedY = y; }
        });

        try (MemoryStack stack = stackPush()) {
            IntBuffer pw = stack.mallocInt(1);
            IntBuffer ph = stack.mallocInt(1);
            glfwGetWindowSize(window, pw, ph);
            windowedW = pw.get(0);
            windowedH = ph.get(0);
            IntBuffer px = stack.mallocInt(1);
            IntBuffer py = stack.mallocInt(1);
            glfwGetWindowPos(window, px, py);
            windowedX = px.get(0);
            windowedY = py.get(0);
        }

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);

        glfwShowWindow(window);

        if (settings.isFullscreen()) {
            setFullscreen(true);
        }
    }

    public static void terminate() { glfwTerminate(); }
    public static boolean isShouldClose() { return glfwWindowShouldClose(window); }
    public static void setShouldClose(boolean flag) { glfwSetWindowShouldClose(window, flag); }

    public static void clear() { glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); }
    public static void swapBuffers() { glfwSwapBuffers(window); }

    public static void setWidth(int width) { Window.width = width; }
    public static void setHeight(int height) { Window.height = height; }

    public static void setTitle(String title) {
        Window.title = title;
        glfwSetWindowTitle(window, title);
    }

    public static void setVsync(boolean flag) {
        Window.vsync = flag;
        glfwSwapInterval(flag ? 1 : 0);
    }

    public static int scaledWidth()  { return Math.ceilDiv(getWidth(),  Math.max(CraftEngineConfig.guiScale, 1)); }
    public static int scaledHeight() { return Math.ceilDiv(getHeight(), Math.max(CraftEngineConfig.guiScale, 1)); }

    public static void setFullscreen(boolean enable) {
        if (fullscreen == enable) return;

        if (enable) {
            try (MemoryStack stack = stackPush()) {
                IntBuffer px = stack.mallocInt(1);
                IntBuffer py = stack.mallocInt(1);
                IntBuffer pw = stack.mallocInt(1);
                IntBuffer ph = stack.mallocInt(1);
                glfwGetWindowPos(window, px, py);
                glfwGetWindowSize(window, pw, ph);
                windowedX = px.get(0);
                windowedY = py.get(0);
                windowedW = pw.get(0);
                windowedH = ph.get(0);
            }

            long monitor = pickBestMonitorForWindow();
            if (monitor == NULL) monitor = glfwGetPrimaryMonitor();
            GLFWVidMode mode = glfwGetVideoMode(monitor);
            glfwSetWindowMonitor(window, monitor, 0, 0, mode.width(), mode.height(), mode.refreshRate());

            width  = mode.width();
            height = mode.height();
            fullscreen = true;
        } else {
            long monitor = pickBestMonitorFor(windowedX, windowedY, windowedW, windowedH);
            if (monitor == NULL) monitor = glfwGetPrimaryMonitor();
            glfwSetWindowMonitor(window, NULL, windowedX, windowedY, windowedW, windowedH, 0);

            width  = windowedW;
            height = windowedH;
            fullscreen = false;
        }

        try (MemoryStack stack = stackPush()) {
            IntBuffer fbW = stack.mallocInt(1);
            IntBuffer fbH = stack.mallocInt(1);
            glfwGetFramebufferSize(window, fbW, fbH);
            glViewport(0, 0, fbW.get(0), fbH.get(0));
        }
    }

    public static void toggleFullscreen() { setFullscreen(!fullscreen); }

    public static Path takeScreenshot() {
        String ts = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        return takeScreenshot(Paths.get("screenshots", "screenshot_" + ts + ".png"));
    }

    public static Path takeScreenshot(Path outPath) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer fbW = stack.mallocInt(1);
            IntBuffer fbH = stack.mallocInt(1);
            glfwGetFramebufferSize(window, fbW, fbH);
            int width  = fbW.get(0);
            int height = fbH.get(0);

            int prevPack = glGetInteger(GL_PACK_ALIGNMENT);
            glPixelStorei(GL_PACK_ALIGNMENT, 1);
            ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * 4);
            glReadBuffer(GL_BACK);
            glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
            glPixelStorei(GL_PACK_ALIGNMENT, prevPack);

            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            int[] dst = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();

            int stride = width * 4;
            for (int y = 0; y < height; y++) {
                int srcY = height - 1 - y;
                int srcOff = srcY * stride;
                for (int x = 0; x < width; x++) {
                    int i = srcOff + x * 4;
                    int r = pixels.get(i)   & 0xFF;
                    int g = pixels.get(i+1) & 0xFF;
                    int b = pixels.get(i+2) & 0xFF;
                    int a = pixels.get(i+3) & 0xFF;
                    dst[y * width + x] = (a << 24) | (r << 16) | (g << 8) | b;
                }
            }

            File out = outPath.toFile();
            File parent = out.getParentFile();
            if (parent != null) parent.mkdirs();
            javax.imageio.ImageIO.write(img, "PNG", out);

            return outPath;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private static void centerOnPrimaryMonitor() {
        long primary = glfwGetPrimaryMonitor();
        GLFWVidMode mode = glfwGetVideoMode(primary);
        try (MemoryStack stack = stackPush()) {
            IntBuffer pW = stack.mallocInt(1);
            IntBuffer pH = stack.mallocInt(1);
            glfwGetWindowSize(window, pW, pH);
            int w = pW.get(0), h = pH.get(0);
            glfwSetWindowPos(window, (mode.width() - w) / 2, (mode.height() - h) / 2);
        }
    }

    private static long pickBestMonitorForWindow() {
        try (MemoryStack stack = stackPush()) {
            IntBuffer wx = stack.mallocInt(1);
            IntBuffer wy = stack.mallocInt(1);
            IntBuffer ww = stack.mallocInt(1);
            IntBuffer wh = stack.mallocInt(1);
            glfwGetWindowPos(window, wx, wy);
            glfwGetWindowSize(window, ww, wh);
            return pickBestMonitorFor(wx.get(0), wy.get(0), ww.get(0), wh.get(0));
        }
    }

    private static long pickBestMonitorFor(int wx, int wy, int ww, int wh) {
        PointerBuffer monitors = glfwGetMonitors();
        if (monitors == null) return NULL;

        long bestMonitor = NULL;
        int bestArea = -1;

        for (int i = 0; i < monitors.capacity(); i++) {
            long m = monitors.get(i);
            GLFWVidMode mode = glfwGetVideoMode(m);
            try (MemoryStack stack = stackPush()) {
                IntBuffer mx = stack.mallocInt(1);
                IntBuffer my = stack.mallocInt(1);
                glfwGetMonitorPos(m, mx, my);

                int monX = mx.get(0);
                int monY = my.get(0);
                int monW = mode.width();
                int monH = mode.height();

                int overlap = rectOverlapArea(wx, wy, ww, wh, monX, monY, monW, monH);
                if (overlap > bestArea) {
                    bestArea = overlap;
                    bestMonitor = m;
                }
            }
        }
        return bestMonitor;
    }

    private static int rectOverlapArea(int ax, int ay, int aw, int ah,
                                       int bx, int by, int bw, int bh) {
        int x1 = Math.max(ax, bx);
        int y1 = Math.max(ay, by);
        int x2 = Math.min(ax + aw, bx + bw);
        int y2 = Math.min(ay + ah, by + bh);
        int w = x2 - x1;
        int h = y2 - y1;
        return (w > 0 && h > 0) ? w * h : 0;
    }
}