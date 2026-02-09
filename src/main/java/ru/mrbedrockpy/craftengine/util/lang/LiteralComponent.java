package ru.mrbedrockpy.craftengine.util.lang;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
@Setter
public final class LiteralComponent extends BaseComponent {

    private String text;

    public LiteralComponent(String text) {
        this.text = text;
    }

    @Override
    public Style getStyle() {
        return null;
    }

    @Override
    public List<Component> getChildren() {
        return List.of();
    }

    @Override
    public Component withStyle(Consumer<Style> mut) {
        return null;
    }

    @Override
    public Map<String, Object> toJson() {
        Map<String, Object> m = baseJson();
        m.put("text", text);
        return m;
    }
}
