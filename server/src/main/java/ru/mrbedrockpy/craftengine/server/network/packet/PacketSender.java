package ru.mrbedrockpy.craftengine.server.network.packet;

public interface PacketSender {
    void send(Packet packet);
    void sendNow(Packet packet);
    void flush();
    void close(String reason);
    boolean isOpen();
}
