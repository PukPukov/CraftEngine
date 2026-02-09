package ru.mrbedrockpy.craftengine.event.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.mrbedrockpy.craftengine.event.Event;
import ru.mrbedrockpy.craftengine.render.window.Input;

@AllArgsConstructor
@Getter
public class MouseClickEvent extends Event {
    private final Input.Layer layer;
    private final int button;
    private final double x;
    private final double y;
}
