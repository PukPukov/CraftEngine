package ru.mrbedrockpy.craftengine.server;

import ru.mrbedrockpy.craftengine.server.network.ConcurrentQueue;
import ru.mrbedrockpy.craftengine.server.network.NetworkManager;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketRegistry;
import ru.mrbedrockpy.craftengine.server.network.packet.ServerPacketHandler;
import ru.mrbedrockpy.craftengine.server.util.chat.ChatManager;

public final class CraftEngineServer extends Server {

    private final int port;

    public CraftEngineServer(int port, ServerPacketHandler handler) {
        super(handler);
        this.port = port;
    }

    @Override
    protected NetworkManager createNetworkManager(ConcurrentQueue<IncomingPacket> in, PacketRegistry reg) {
        return NetworkManager.server(port, in, reg);
    }
}