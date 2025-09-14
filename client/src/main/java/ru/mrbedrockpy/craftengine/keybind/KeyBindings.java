package ru.mrbedrockpy.craftengine.keybind;

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
}
