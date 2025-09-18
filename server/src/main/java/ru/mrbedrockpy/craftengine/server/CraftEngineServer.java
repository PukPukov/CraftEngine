package ru.mrbedrockpy.craftengine.server;

import ru.mrbedrockpy.craftengine.server.network.ConcurrentQueue;
import ru.mrbedrockpy.craftengine.server.network.NetworkManager;
import ru.mrbedrockpy.craftengine.server.network.packet.Packet;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketRegistry;

public final class CraftEngineServer extends Server {

    private final int port;

    public CraftEngineServer(int port) {
        this.port = port;
    }

    @Override
    protected NetworkManager createNetworkManager(ConcurrentQueue<IncomingPacket> in, PacketRegistry reg) {
        return new NetworkManager(port, in, reg);
    }
}