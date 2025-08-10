package ru.mrbedrockpy.craftengine.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class MouseClickEvent extends Event {
    private final int button;
    private final double x;
    private final double y;
}
