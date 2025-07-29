package ru.mrbedrockpy.craftengine.event;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public abstract class Event {
    @Getter
    private boolean cancelled = false;
    public void cancel() {
        this.cancelled = true;
    }
}
