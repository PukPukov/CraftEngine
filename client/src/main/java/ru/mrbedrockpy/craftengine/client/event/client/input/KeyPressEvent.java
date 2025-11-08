package ru.mrbedrockpy.craftengine.client.event.client.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.mrbedrockpy.craftengine.client.event.Event;

@AllArgsConstructor
@Getter
public class KeyPressEvent extends Event {
    private final int keyCode;
    private final int scanCode;
    private final int inputAction;
    private final int mods;
}
