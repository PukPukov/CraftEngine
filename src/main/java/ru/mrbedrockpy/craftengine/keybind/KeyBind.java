package ru.mrbedrockpy.craftengine.keybind;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.mrbedrockpy.craftengine.renderer.window.Input;

@Getter
@AllArgsConstructor
public class KeyBind {
    public record Key(Type type, int code) {
        public enum Type { KEYBOARD, MOUSE }
    }

    private final Input.Layer layer;
    @Getter @Setter
    private Key key;
    private final String name, cat;

    public KeyBind(Input.Layer layer, String name, String cat, int keyCode){
        this.layer = layer;
        this.name = name;
        this.cat = cat;
        this.key = new Key(Key.Type.KEYBOARD, keyCode);
    }

    public boolean isPressed() {
        return switch (key.type()) {
            case KEYBOARD -> Input.isPressed(layer, key.code());
            case MOUSE    -> Input.isClicked(layer, key.code());
        };
    }

    public boolean wasPressed() {
        return switch (key.type()) {
            case KEYBOARD -> Input.wasPressed(layer, key.code());
            case MOUSE    -> Input.wasClicked(layer, key.code());
        };
    }
}