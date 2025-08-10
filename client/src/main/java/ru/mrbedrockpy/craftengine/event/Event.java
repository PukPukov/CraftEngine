package ru.mrbedrockpy.craftengine.event;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public abstract class Event {

    @Getter @Setter private boolean cancelled = false;

}
