package ru.mrbedrockpy.craftengine.event.client;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.mrbedrockpy.craftengine.event.Event;

@AllArgsConstructor
@Getter
public class KeyPressEvent extends Event {
    private final int keyCode;
    private final int scanCode;
    private final int inputAction;
    private final int mods;
}
