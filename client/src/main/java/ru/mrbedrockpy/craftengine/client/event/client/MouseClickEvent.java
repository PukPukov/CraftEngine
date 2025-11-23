package ru.mrbedrockpy.craftengine.client.event.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.mrbedrockpy.craftengine.client.event.Event;
import ru.mrbedrockpy.renderer.window.Input;

@AllArgsConstructor
@Getter
public class MouseClickEvent extends Event {
    private final Input.Layer layer;
    private final int button;
    private final double x;
    private final double y;
}
