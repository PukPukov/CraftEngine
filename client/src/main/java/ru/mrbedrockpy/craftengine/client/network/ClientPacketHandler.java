package ru.mrbedrockpy.craftengine.client.network;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodec;
import ru.mrbedrockpy.craftengine.server.network.packet.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

@RequiredArgsConstructor
public class ClientPacketHandler implements PacketHandler<Packet, ClientHandleContext> {
    private final PacketRegistry registry;
    private final Map<Integer, BiConsumer<ClientHandleContext, Packet>> handles = new HashMap<>();
    public <P extends Packet> void register(Class<P> clazz, BiConsumer<ClientHandleContext, Packet> handler){
        int id = registry.idOf(PacketDirection.S2C, clazz);
        handles.put(id, handler);
    }

    @Override
    public void handle(ClientHandleContext ctx, Packet packet) {
        handles.get(registry.idOf(PacketDirection.S2C, packet.getClass())).accept(ctx, packet);
    }
}

