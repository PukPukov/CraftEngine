package ru.mrbedrockpy.craftengine.server.util.chat;

import lombok.Getter;
import ru.mrbedrockpy.craftengine.core.world.entity.PlayerEntity;

import java.util.*;

public class ChatManager {

    @Getter private final List<String> messages = new ArrayList<>();;

    public void onMessage(String pn, String message) {
        messages.add(pn + "\uE000" + message);
    }
}
