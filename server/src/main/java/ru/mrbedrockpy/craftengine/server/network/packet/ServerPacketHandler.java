package ru.mrbedrockpy.craftengine.server.network.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import ru.mrbedrockpy.craftengine.server.Server;
import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodec;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
public final class ServerPacketHandler implements PacketHandler<Packet, ServerHandleContext> {
    private final PacketRegistry registry;

    private final Map<Integer, BiConsumer<ServerHandleContext, Packet>> handles = new HashMap<>();

    public <P extends Packet> void register(Class<P> clazz, BiConsumer<ServerHandleContext, Packet> handler) {
        int id = registry.idOf(PacketDirection.C2S, clazz);
        handles.put(id, handler);
    }

    @Override
    public void handle(ServerHandleContext ctx, Packet packet) {
        handles.get(registry.idOf(PacketDirection.C2S, packet.getClass())).accept(ctx, packet);
    }
}