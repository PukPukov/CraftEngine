package ru.mrbedrockpy.renderer.window;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.glViewport;

public class Input {
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

    public static Map<Integer, Runnable> onPress = new ConcurrentHashMap<>();
    public static Map<Integer, Runnable> onRelease = new ConcurrentHashMap<>();

    public enum Layer {
        GAME      (   0, true,  true,  true),   // Игровой: забирает клавиатуру/мышь, курсор залочен
        UI        ( 100, true,  true,  false);  // UI-меню: забирает ввод, курсор свободен

        public final int priority;
        public final boolean captureKeyboard;
        public final boolean captureMouse;
        public final boolean lockCursorByDefault;

        Layer(int priority, boolean captureKeyboard, boolean captureMouse, boolean lockCursorByDefault) {
            this.priority = priority;
            this.captureKeyboard = captureKeyboard;
            this.captureMouse = captureMouse;
            this.lockCursorByDefault = lockCursorByDefault;
        }
    }

    // Активный стек слоёв: верхний = текущий
    private static final Deque<Layer> layerStack = new ArrayDeque<>();

    // Отдельные коллбэки по слоям
    private static final Map<Layer, Map<Integer, Runnable>> onPressByLayer   = new ConcurrentHashMap<>();
    private static final Map<Layer, Map<Integer, Runnable>> onReleaseByLayer = new ConcurrentHashMap<>();

    public static void onPress(Layer layer, int key, Runnable r) {
        onPressByLayer.computeIfAbsent(layer, l -> new ConcurrentHashMap<>()).put(key, r);
    }
    public static void onRelease(Layer layer, int key, Runnable r) {
        onReleaseByLayer.computeIfAbsent(layer, l -> new ConcurrentHashMap<>()).put(key, r);
    }

    public static void pullEvents() {
        current++;
        deltaX = 0f;
        deltaY = 0f;
        glfwPollEvents();
    }

    public static void pushLayer(Layer layer) {
        layerStack.push(layer);
        applyCursorPolicy();
    }
    public static void popLayer() {
        if (!layerStack.isEmpty()) {
            layerStack.pop();
            applyCursorPolicy();
        }
    }
    public static @Nullable Layer currentLayer() {
        return layerStack.peek();
    }

    private static void applyCursorPolicy() {
        Layer top = currentLayer();
        boolean lock = top != null ? top.lockCursorByDefault : true;
        setCursorLocked(lock);
    }

    private static void keyCallback(long window, int key, int scancode, int inputAction, int mode) {
        Layer top = currentLayer();
        boolean deliverToGlobal = (top == null || !top.captureKeyboard);

        if (inputAction == GLFW_PRESS) {
            keys[key] = true;
            frames[key] = current;

            if (!deliverToGlobal) {
                Runnable r = onPressByLayer.getOrDefault(top, Map.of()).get(key);
                if (r != null) r.run();
            } else {
                Runnable r = onPress.get(key);
                if (r != null) r.run();
            }
        } else if (inputAction == GLFW_RELEASE) {
            keys[key] = false;
            frames[key] = current;

            if (!deliverToGlobal) {
                Runnable r = onReleaseByLayer.getOrDefault(top, Map.of()).get(key);
                if (r != null) r.run();
            } else {
                Runnable r = onRelease.get(key);
                if (r != null) r.run();
            }
        }
    }

    private static void mouseButtonCallback(long window, int button, int action, int mode) {
        Layer top = currentLayer();
        boolean deliverToGlobal = (top == null || !top.captureMouse);

        int idx = MOUSE_BUTTONS_OFFSET + button;
        if (action == GLFW_PRESS){
            keys[idx] = true;
            frames[idx] = current;

            if (!deliverToGlobal) {
                Runnable r = onPressByLayer.getOrDefault(top, Map.of()).get(idx);
                if (r != null) r.run();
            } else {
                Runnable r = onPress.get(idx);
                if (r != null) r.run();
            }
        } else if (action == GLFW_RELEASE){
            keys[idx] = false;
            frames[idx] = current;

            if (!deliverToGlobal) {
                Runnable r = onReleaseByLayer.getOrDefault(top, Map.of()).get(idx);
                if (r != null) r.run();
            } else {
                Runnable r = onRelease.get(idx);
                if (r != null) r.run();
            }
        }
    }

    private static void cursorPosCallback(long window, double xpos, double ypos) {
        if (cursorStarted) {
            deltaX += xpos - x;
            deltaY += ypos - y;
        } else {
            cursorStarted = true;
        }
        x = xpos;
        y = ypos;
    }

    public static void initialize() {
        long window = Window.getWindow();
        keys = new boolean[KEYS];
        frames = new long[KEYS];
        Arrays.fill(keys, false);
        Arrays.fill(frames, 0);

        glfwSetKeyCallback(window, Input::keyCallback);
        glfwSetMouseButtonCallback(window, Input::mouseButtonCallback);
        glfwSetCursorPosCallback(window, Input::cursorPosCallback);
        glfwSetWindowSizeCallback(window, Input::windowSizeCallback);

        pushLayer(Layer.GAME);
    }

    public static void setCursorLocked(boolean flag) {
        glfwSetInputMode(Window.getWindow(), GLFW_CURSOR, flag ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
    }

    private static void windowSizeCallback(long window, int width, int height) {
        glViewport(0, 0, width, height);
        Window.setWidth(width);
        Window.setHeight(height);
    }

    // is - зажат ли, was - был ли нажат
    public static boolean isPressed(Layer layer, int key) {
        if (currentLayer() != layer) return false;
        return keys[key];
    }
    public static boolean wasPressed(Layer layer, int key) {
        if (currentLayer() != layer) return false;
        return keys[key] && frames[key] == current;
    }
    public static boolean isClicked(Layer layer, int button) {
        if (currentLayer() != layer) return false;
        return keys[MOUSE_BUTTONS_OFFSET + button];
    }
    public static boolean wasClicked(Layer layer, int button) {
        if (currentLayer() != layer) return false;
        return keys[MOUSE_BUTTONS_OFFSET + button] && frames[MOUSE_BUTTONS_OFFSET + button] == current;
    }
    public static boolean wasKeyPressedThisFrame(int key) {
        return keys[key] && frames[key] == current;
    }
    public static boolean wasMouseClickedThisFrame(int button) {
        int idx = MOUSE_BUTTONS_OFFSET + button;
        return keys[idx] && frames[idx] == current;
    }
}