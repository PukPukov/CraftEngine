package ru.mrbedrockpy.craftengine.event.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.mrbedrockpy.craftengine.event.Event;

@AllArgsConstructor
@Getter
public class CharTypeEvent extends Event {
    private final int ch;
    private final int mods;
}
