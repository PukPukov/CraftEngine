package ru.mrbedrockpy.craftengine.client.network.game;

import ru.mrbedrockpy.craftengine.client.network.packet.Packet;

public interface GameClientListener {
    void onPacket(Packet packet);
    default void onConnected() {}
    default void onDisconnected(String reason, Throwable cause) {}
}