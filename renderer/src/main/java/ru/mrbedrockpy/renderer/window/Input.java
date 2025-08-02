package ru.mrbedrockpy.renderer.window;

import lombok.Getter;

import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.glViewport;

public class Input {

    private static boolean guiOpen = false;

    public static void openGUI() {
        guiOpen = true;
        Input.setCursorLocked(false);
    }

    public static void closeGUI() {
        guiOpen = false;
        Input.setCursorLocked(true);
    }

    public static boolean isGUIOpen() {
        return guiOpen;
    }

    public static final int KEYS = 1032;
    public static final int MOUSE_BUTTONS_OFFSET = 1024;

    private static boolean[] keys;
    private static long[] frames;

    private static long current = 0;

    @Getter private static double deltaX = 0f;
    @Getter private static double deltaY = 0f;

    @Getter private static double x = 0f;
    @Getter private static double y = 0f;

    private static boolean cursorStarted = false;

    private static void keyCallback(long window, int key, int scancode, int action, int mode) {
        if (action == GLFW_PRESS) {
            keys[key] = true;
            frames[key] = current;
        } else if (action == GLFW_RELEASE) {
            keys[key] = false;
            frames[key] = current;
        }
    }

    private static void mouseButtonCallback(long window, int button, int action, int mode) {
        if (action == GLFW_PRESS){
            keys[MOUSE_BUTTONS_OFFSET + button] = true;
            frames[MOUSE_BUTTONS_OFFSET + button] = current;
        }
        else if (action == GLFW_RELEASE){
            keys[MOUSE_BUTTONS_OFFSET + button] = false;
            frames[MOUSE_BUTTONS_OFFSET + button] = current;
        }
    }

    private static void cursorPosCallback(long window, double xpos, double ypos) {
        if (cursorStarted) {
            deltaX += xpos - x;
            deltaY += ypos - y;
        }
        else {
            cursorStarted = true;
        }
        x = xpos;
        y = ypos;
    }

    private static void windowSizeCallback(long window, int width, int height) {
        glViewport(0, 0, width, height);
        Window.setWidth(width);
        Window.setHeight(height);
    }

    public static void initialize() {
        long window = Window.window();
        keys = new boolean[KEYS];
        frames = new long[KEYS];

        Arrays.fill(keys, false);
        Arrays.fill(frames, 0);

        glfwSetKeyCallback(window, Input::keyCallback);
        glfwSetMouseButtonCallback(window, Input::mouseButtonCallback);
        glfwSetCursorPosCallback(window, Input::cursorPosCallback);
        glfwSetWindowSizeCallback(window, Input::windowSizeCallback);

        setCursorLocked(true);
    }

    public static void pullEvents() {
        current++;
        deltaX = 0f;
        deltaY = 0f;
        glfwPollEvents();
    }

    public static void setCursorLocked(boolean flag) {
        glfwSetInputMode(Window.window(), GLFW_CURSOR, flag ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
    }

    public static boolean pressed(int key) {
        return keys[key];
    }

    public static boolean jpressed(int key) {
        return keys[key] && frames[key] == current;
    }

    public static boolean clicked(int button) {
        return keys[MOUSE_BUTTONS_OFFSET + button];
    }

    public static boolean jclicked(int button) {
        return keys[MOUSE_BUTTONS_OFFSET + button] && frames[MOUSE_BUTTONS_OFFSET + button] == current;
    }
}