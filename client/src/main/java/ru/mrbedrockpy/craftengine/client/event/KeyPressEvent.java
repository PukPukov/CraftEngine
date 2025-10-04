package ru.mrbedrockpy.craftengine.client.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class KeyPressEvent extends Event {
    private final int keyCode;
}
