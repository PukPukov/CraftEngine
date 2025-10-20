package ru.mrbedrockpy.craftengine.client.keybind;

import org.lwjgl.glfw.GLFW;
import ru.mrbedrockpy.renderer.window.Input;

public class KeyBindings {
    public static final KeyBind MOVE_FORWARD = KeyBindingsHelper.register(new KeyBind(Input.Layer.GAME, "forward", "game", GLFW.GLFW_KEY_W));
    public static final KeyBind MOVE_BACK = KeyBindingsHelper.register(new KeyBind(Input.Layer.GAME, "back", "game", GLFW.GLFW_KEY_S));
    public static final KeyBind MOVE_LEFT = KeyBindingsHelper.register(new KeyBind(Input.Layer.GAME, "left", "game", GLFW.GLFW_KEY_A));
    public static final KeyBind MOVE_RIGHT = KeyBindingsHelper.register(new KeyBind(Input.Layer.GAME, "right", "game", GLFW.GLFW_KEY_D));
    public static final KeyBind JUMP = KeyBindingsHelper.register(new KeyBind(Input.Layer.GAME, "jump", "game", GLFW.GLFW_KEY_SPACE));
    public static final KeyBind SHIFT = KeyBindingsHelper.register(new KeyBind(Input.Layer.GAME, "shift", "game", GLFW.GLFW_KEY_LEFT_SHIFT));
    public static final KeyBind SPRINT = KeyBindingsHelper.register(new KeyBind(Input.Layer.GAME, "sprint", "game", GLFW.GLFW_KEY_LEFT_CONTROL));
    public static final KeyBind ATTACK = KeyBindingsHelper.register(new KeyBind(Input.Layer.GAME, new KeyBind.Key(KeyBind.Key.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_LEFT), "attack", "game"));
    public static final KeyBind BUILD = KeyBindingsHelper.register(new KeyBind(Input.Layer.GAME, new KeyBind.Key(KeyBind.Key.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_RIGHT), "build", "game"));
    public static final KeyBind S1 = KeyBindingsHelper.register(new KeyBind(Input.Layer.GAME, "1", "inventory", GLFW.GLFW_KEY_1));
    public static final KeyBind S2 = KeyBindingsHelper.register(new KeyBind(Input.Layer.GAME, "2", "inventory", GLFW.GLFW_KEY_2));
    public static final KeyBind S3 = KeyBindingsHelper.register(new KeyBind(Input.Layer.GAME, "3", "inventory", GLFW.GLFW_KEY_3));
    public static final KeyBind S4 = KeyBindingsHelper.register(new KeyBind(Input.Layer.GAME, "4", "inventory", GLFW.GLFW_KEY_4));
    public static final KeyBind S5 = KeyBindingsHelper.register(new KeyBind(Input.Layer.GAME, "5", "inventory", GLFW.GLFW_KEY_5));
    public static final KeyBind S6 = KeyBindingsHelper.register(new KeyBind(Input.Layer.GAME, "6", "inventory", GLFW.GLFW_KEY_6));
    public static final KeyBind S7 = KeyBindingsHelper.register(new KeyBind(Input.Layer.GAME, "7", "inventory", GLFW.GLFW_KEY_7));
    public static final KeyBind S8 = KeyBindingsHelper.register(new KeyBind(Input.Layer.GAME, "8", "inventory", GLFW.GLFW_KEY_8));
    public static final KeyBind S9 = KeyBindingsHelper.register(new KeyBind(Input.Layer.GAME, "9", "inventory", GLFW.GLFW_KEY_9));
    public static void register(){}
}
