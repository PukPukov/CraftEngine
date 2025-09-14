package ru.mrbedrockpy.craftengine.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.mrbedrockpy.renderer.window.Input;

@AllArgsConstructor
@Getter
public class MouseClickEvent extends Event {
    private final Input.Layer layer;
    private final int button;
    private final double x;
    private final double y;
}
