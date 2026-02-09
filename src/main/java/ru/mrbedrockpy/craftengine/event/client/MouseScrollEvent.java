package ru.mrbedrockpy.craftengine.event.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.mrbedrockpy.craftengine.event.Event;

@AllArgsConstructor
@Getter
public class MouseScrollEvent extends Event {
    private final double scrollX, scrollY;
}
