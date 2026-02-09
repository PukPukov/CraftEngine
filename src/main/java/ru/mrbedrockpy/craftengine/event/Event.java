package ru.mrbedrockpy.craftengine.event;

import lombok.Getter;
import lombok.Setter;

public abstract class Event {
    @Getter @Setter private boolean cancelled = false;
}
