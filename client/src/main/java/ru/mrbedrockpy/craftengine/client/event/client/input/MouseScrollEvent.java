package ru.mrbedrockpy.craftengine.client.event.client.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.mrbedrockpy.craftengine.client.event.Event;

@AllArgsConstructor
@Getter
public class MouseScrollEvent extends Event {
    private final double scrollX, scrollY;
}
