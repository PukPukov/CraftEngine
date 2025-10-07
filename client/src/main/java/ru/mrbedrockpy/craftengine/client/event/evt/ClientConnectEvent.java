package ru.mrbedrockpy.craftengine.client.event.evt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.mrbedrockpy.craftengine.client.event.Event;
import ru.mrbedrockpy.craftengine.server.network.packet.PlayerConnection;

@AllArgsConstructor
@Getter
public class ClientConnectEvent extends Event {
    private final PlayerConnection connection;
    private final String host;
    private final int port;
}
