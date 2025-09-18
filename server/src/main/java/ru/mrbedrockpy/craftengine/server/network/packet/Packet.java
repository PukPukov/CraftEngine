package ru.mrbedrockpy.craftengine.server.network.packet;

@FunctionalInterface
public interface Packet {
    void handle(PacketHandleContext ctx);
}