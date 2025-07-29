package ru.mrbedrockpy.craftengine.event;

import lombok.Getter;

@Getter
public class MouseClickEvent extends Event {
    private final int button;
    private final double x;
    private final double y;

    public MouseClickEvent(int button, double x, double y) {
        this.button = button;
        this.x = x;
        this.y = y;
    }

}
