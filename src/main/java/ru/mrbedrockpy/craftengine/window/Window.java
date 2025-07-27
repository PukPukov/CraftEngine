package ru.mrbedrockpy.craftengine.window;

import lombok.Getter;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

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

    public static void initialize(WindowSettings settings) {

        Window.width = settings.getWidth();
        Window.height = settings.getHeight();
        Window.title = settings.getTitle();
        Window.vsync = settings.isVsync();
        Window.fullscreen = settings.isFullscreen();

        glfwInitHint(GLFW_PLATFORM, GLFW_PLATFORM_X11);

        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 6);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL) throw new IllegalStateException("Failed to create the GLFW window");

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(window, pWidth, pHeight);

            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);
        setVsync(vsync);
        glfwShowWindow(window);
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);
    }

    public static void terminate() {
        glfwTerminate();
    }

    public static boolean isShouldClose() {
        return glfwWindowShouldClose(window);
    }

    public static void setShouldClose(boolean flag) {
        glfwSetWindowShouldClose(window, flag);
    }

    public static void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//        glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void swapBuffers() {
        glfwSwapBuffers(window);
    }

    public static void setWidth(int width) {
        Window.width = width;
    }

    public static void setHeight(int height) {
        Window.height = height;
    }

    public static void setTitle(String title) {
        glfwSetWindowTitle(window, title);
        Window.title = title;
    }

    public static void setVsync(boolean flag) {
        glfwSwapInterval(flag ? 1 : 0);
    }
}