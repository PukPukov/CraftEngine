package ru.mrbedrockpy.renderer.window;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.glViewport;

public class Input{
    public static final int KEYS = 1032;
    public static final int MOUSE_BUTTONS_OFFSET = 1024;

    private static boolean[] keys;
    private static long[] frames;

    private static long current = 0;

    @Getter
    private static double scrollX = 0.0;
    @Getter
    private static double scrollY = 0.0;

    @Getter
    private static double deltaX = 0f;
    @Getter
    private static double deltaY = 0f;

    private static double x = 0f;
    private static double y = 0f;

    private static boolean cursorStarted = false;

    public static Map<Integer, Runnable> onPress = new ConcurrentHashMap<>();
    public static Map<Integer, Runnable> onRelease = new ConcurrentHashMap<>();
    public static List<Consumer<KeyCallback>> onPressAny = new ArrayList<>();
    public static List<Consumer<ScrollCallback>> onScrollAny = new ArrayList<>();
    public static List<Consumer<CharCallBack>> onCharAny = new ArrayList<>();

    private static final Map<Layer, List<Consumer<ScrollCallback>>> onScrollByLayer = new ConcurrentHashMap<>();

    public static void onScroll(Layer layer, Consumer<ScrollCallback> cb) {
        onScrollByLayer.computeIfAbsent(layer, l -> new ArrayList<>()).add(cb);
    }

    public static int getSX() {
        return Window.scale().toLogicalX(Math.round((float) getX()));
    }

    public static int getSY() {
        return Window.scale().toLogicalY(Math.round((float) getY()));
    }

    public static double getX() {
        return getCurrentLayer().cursorLocked ? -1: x;
    }

    public static double getY() {
        return getCurrentLayer().cursorLocked ? -1: y;
    }

    public record ScrollCallback(double xoffset, double yoffset) {
    }

    public record CharCallBack(int c, int mods) {
    }

    public record KeyCallback(int key, int scancode, int inputAction, int mods) {
    }

    public enum Layer {
        GAME(true, true, true),
        UI(true, true, false);

        public final boolean captureKeyboard;
        public final boolean captureMouse;
        public final boolean cursorLocked;

        Layer(boolean captureKeyboard, boolean captureMouse, boolean lockCursorByDefault) {
            this.captureKeyboard = captureKeyboard;
            this.captureMouse = captureMouse;
            this.cursorLocked = lockCursorByDefault;
        }
    }

    // Активный стек слоёв: верхний = текущий
    @Getter
    private static Layer currentLayer = Layer.GAME;

    // Отдельные коллбэки по слоям
    private static final Map<Layer, Map<Integer, Runnable>> onPressByLayer = new ConcurrentHashMap<>();
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
        scrollX = 0f;
        scrollY = 0f;
        glfwPollEvents();
    }

    public static void setLayer(@Nullable Layer layer) {
        currentLayer = layer == null ? Layer.GAME : layer;
        applyCursorPolicy();
    }

    public static String getKeyName(int key, int scanCode) {
        return glfwGetKeyName(key, scanCode) == null ? "None" : glfwGetKeyName(key, scanCode);
    }

    private static void applyCursorPolicy() {
        Layer top = getCurrentLayer();
        boolean lock = top.cursorLocked;
        setCursorLocked(lock);
    }

    private static void keyCallback(long window, int key, int scancode, int inputAction, int mods) {
        Layer top = getCurrentLayer();
        boolean deliverToGlobal = (top == null || !top.captureKeyboard);

        if (key < 0 || key >= KEYS) return; // защита от выхода за массив

        if (inputAction == GLFW_PRESS) {
            keys[key] = true;
            frames[key] = current;
            for (Consumer<KeyCallback> cb : onPressAny) {
                cb.accept(new KeyCallback(key, scancode, inputAction, mods));
            }

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
        Layer top = getCurrentLayer();
        boolean deliverToGlobal = (top == null || !top.captureMouse);

        int idx = MOUSE_BUTTONS_OFFSET + button;
        if (action == GLFW_PRESS) {
            keys[idx] = true;
            frames[idx] = current;

            if (!deliverToGlobal) {
                Runnable r = onPressByLayer.getOrDefault(top, Map.of()).get(idx);
                if (r != null) r.run();
            } else {
                Runnable r = onPress.get(idx);
                if (r != null) r.run();
            }
        } else if (action == GLFW_RELEASE) {
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

    private static void scrollCallback(long window, double xoffset, double yoffset) {
        Layer top = getCurrentLayer();
        boolean deliverToGlobal = (top == null || !top.captureMouse);

        scrollX += xoffset;
        scrollY += yoffset;

        ScrollCallback evt = new ScrollCallback(xoffset, yoffset);

        if (!deliverToGlobal) {
            for (Consumer<ScrollCallback> cb : onScrollByLayer.getOrDefault(top, List.of())) {
                cb.accept(evt);
            }
        }
        for (Consumer<ScrollCallback> cb : onScrollAny) {
            cb.accept(evt);
        }
    }

    private static void charCallback(long window, int c, int mods) {
        CharCallBack evt = new CharCallBack(c, mods);
        for (Consumer<CharCallBack> cb : onCharAny) {
           cb.accept(evt);
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
        glfwSetScrollCallback(window, Input::scrollCallback);
        glfwSetCharModsCallback(window, Input::charCallback);

        setLayer(Layer.GAME);
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
    public static boolean isPressed(int key) {
        return keys[key];
    }

    public static boolean wasPressed(int key) {
        return keys[key] && frames[key] == current;
    }

    public static boolean isClicked(int button) {
        return keys[MOUSE_BUTTONS_OFFSET + button];
    }

    public static boolean wasClicked(int button) {
        return keys[MOUSE_BUTTONS_OFFSET + button] && frames[MOUSE_BUTTONS_OFFSET + button] == current;
    }

    public static boolean isPressed(Layer layer, int key) {
        if (getCurrentLayer() != layer) return false;
        return keys[key];
    }

    public static boolean wasPressed(Layer layer, int key) {
        if (getCurrentLayer() != layer) return false;
        return keys[key] && frames[key] == current;
    }

    public static boolean isClicked(Layer layer, int button) {
        if (getCurrentLayer() != layer) return false;
        return keys[MOUSE_BUTTONS_OFFSET + button];
    }

    public static boolean wasClicked(Layer layer, int button) {
        if (getCurrentLayer() != layer) return false;
        return keys[MOUSE_BUTTONS_OFFSET + button] && frames[MOUSE_BUTTONS_OFFSET + button] == current;
    }
}