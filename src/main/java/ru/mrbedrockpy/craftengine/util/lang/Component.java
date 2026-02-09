package ru.mrbedrockpy.craftengine.util.lang;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface Component {

    Style getStyle();
    List<Component> getChildren();
    Component withStyle(Consumer<Style> mut);
    Component append(Component child);

    Map<String, Object> toJson();

    static Component literal(String text) {
        return new LiteralComponent(text);
    }
    static Component translatable(String key, Object... args) {
        return new TranslatableComponent(key, args);
    }
    static Component keybind(String key) {
        return new LiteralComponent(key);
    }
}
