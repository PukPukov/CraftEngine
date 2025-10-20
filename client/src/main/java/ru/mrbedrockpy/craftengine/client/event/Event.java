package ru.mrbedrockpy.craftengine.client.event;

import lombok.Getter;
import lombok.Setter;

public abstract class Event {

    @Getter @Setter private boolean cancelled = false;

}
