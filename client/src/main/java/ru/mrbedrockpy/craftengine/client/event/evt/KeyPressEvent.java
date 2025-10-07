package ru.mrbedrockpy.craftengine.client.event.evt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.mrbedrockpy.craftengine.client.event.Event;

@AllArgsConstructor
@Getter
public class KeyPressEvent extends Event {
    private final int keyCode;
}
