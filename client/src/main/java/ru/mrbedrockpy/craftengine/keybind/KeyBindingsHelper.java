package ru.mrbedrockpy.craftengine.keybind;

import ru.mrbedrockpy.renderer.window.Input;

import java.util.*;

public class KeyBindingsHelper {

    private static final List<KeyBind> BINDS = new ArrayList<>();
    private static final Map<String, KeyBind> BY_NAME = new HashMap<>();

    public static KeyBind register(KeyBind bind) {
        BINDS.add(bind);
        BY_NAME.put(bind.getName(), bind);
        return bind;
    }

    public static KeyBind get(String name) {
        return BY_NAME.get(name);
    }

    public static List<KeyBind> all() {
        return Collections.unmodifiableList(BINDS);
    }
}
