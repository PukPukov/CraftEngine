package ru.mrbedrockpy.craftengine.render.window;

import lombok.Getter;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import ru.mrbedrockpy.craftengine.data.WindowSettings;
import ru.mrbedrockpy.craftengine.util.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL46C.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

public class Window {

    private static final Logger LOGGER = Logger.getLogger(Window.class);

    @Getter private static long window;
    @Getter private static int width, height;
    @Getter private static String title;
    @Getter private static boolean vsync;
    @Getter private static boolean fullscreen;

    private static int windowedX, windowedY, windowedW, windowedH;

    @Getter private static final ScaleManager scaleManager = new ScaleManager();

    // Callbacks
    private static GLFWErrorCallback errorCallback;
    private static GLFWFramebufferSizeCallback framebufferSizeCallback;
    private static GLFWWindowSizeCallback windowSizeCallback;
    private static GLFWWindowPosCallback windowPosCallback;

    public static void initialize(WindowSettings settings) {
        Window.width  = settings.getWidth();
        Window.height = settings.getHeight();
        Window.title  = settings.getTitle();
        Window.vsync  = settings.isVsync();
        Window.fullscreen = false;

        if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            glfwInitHint(GLFW_PLATFORM, GLFW_PLATFORM_X11);
        }
        
        errorCallback = GLFWErrorCallback.create((error, descriptionPtr) -> {
            String description = MemoryUtil.memASCII(descriptionPtr);
            System.err.println("GLFW Error " + error + ": " + description);
        });
        errorCallback.set();
        scaleManager.setFramebufferSize(width, height);
        initialize0(settings);
    }
    
    private static void initialize0(WindowSettings settings) {
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

        framebufferSizeCallback = glfwSetFramebufferSizeCallback(window, (win, w, h) -> {
            if (w <= 0 || h <= 0) return;
            glViewport(0, 0, w, h);
        });
        windowSizeCallback = glfwSetWindowSizeCallback(window, (win, w, h) -> {
            if (!fullscreen) { windowedW = w; windowedH = h; }
            Window.width = w;
            Window.height = h;
        });
        windowPosCallback = glfwSetWindowPosCallback(window, (win, x, y) -> {
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
        
        if (settings.isFullscreen()) setFullscreen(true);
    }

    public static void terminate() {
        Input.freeCallbacks();
        if (framebufferSizeCallback != null) framebufferSizeCallback.free();
        if (windowSizeCallback != null) windowSizeCallback.free();
        if (windowPosCallback != null) windowPosCallback.free();
        errorCallback.free();
        glfwTerminate();
    }

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

    public static int scaledWidth()  { return scaleManager.logicalWidth(); }
    public static int scaledHeight() { return scaleManager.logicalHeight(); }

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

            long monitor = glfwGetPrimaryMonitor();
            GLFWVidMode mode = glfwGetVideoMode(monitor);
            if (mode == null) {
                Logger.getLogger(Window.class).error("Failed to get video mode");
                return;
            }
            glfwSetWindowMonitor(window, monitor, 0, 0, mode.width(), mode.height(), mode.refreshRate());

            width  = mode.width();
            height = mode.height();
            fullscreen = true;
        } else {
            glfwSetWindowMonitor(window, glfwGetPrimaryMonitor(), windowedX, windowedY, windowedW, windowedH, 0);

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

    public static void toggleFullscreen() {
        setFullscreen(!fullscreen);
    }

    public static void takeScreenshot() {
        Path ts = Paths.get("screenshots", "screenshot_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")) + ".png");
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

            File out = ts.toFile();
            File parent = out.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs()) {
                    LOGGER.error("Failed to create parent directory");
                    return;
                }
            }
            ImageIO.write(img, "PNG", out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void centerOnPrimaryMonitor() {
        long primary = glfwGetPrimaryMonitor();
        GLFWVidMode mode = glfwGetVideoMode(primary);
        if (mode == null) {
            Logger.getLogger(Window.class).error("Failed to get video mode");
            return;
        }
        try (MemoryStack stack = stackPush()) {
            IntBuffer pW = stack.mallocInt(1);
            IntBuffer pH = stack.mallocInt(1);
            glfwGetWindowSize(window, pW, pH);
            int w = pW.get(0), h = pH.get(0);
            glfwSetWindowPos(window, (mode.width() - w) / 2, (mode.height() - h) / 2);
        }
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