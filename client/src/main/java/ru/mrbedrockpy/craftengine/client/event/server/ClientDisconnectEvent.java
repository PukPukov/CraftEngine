package ru.mrbedrockpy.craftengine.client.event.server;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.mrbedrockpy.craftengine.client.event.Event;
import ru.mrbedrockpy.craftengine.server.network.packet.PlayerConnection;

@AllArgsConstructor
@Getter
public class ClientDisconnectEvent extends Event {

    private final PlayerConnection connection;
    private final String host;
    private final int port;
    private final Reason reason;

    @Getter
    @AllArgsConstructor
    public enum Reason {

        KICK(""),
        LEAVE(""),
        DISCONNECT("");

        private final String reason;

    }

}
