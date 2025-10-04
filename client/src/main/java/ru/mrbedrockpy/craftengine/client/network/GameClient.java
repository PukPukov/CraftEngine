package ru.mrbedrockpy.craftengine.client.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Setter;
import ru.mrbedrockpy.craftengine.client.CraftEngineClient;
import ru.mrbedrockpy.craftengine.client.event.ClientConnectEvent;
import ru.mrbedrockpy.craftengine.client.network.aut.GameProfile;
import ru.mrbedrockpy.craftengine.client.network.game.GameClientListener;
import ru.mrbedrockpy.craftengine.server.Server;
import ru.mrbedrockpy.craftengine.server.network.ConcurrentQueue;
import ru.mrbedrockpy.craftengine.server.network.NetworkManager;
import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodec;
import ru.mrbedrockpy.craftengine.server.network.packet.*;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.ClientLoginPacketC2S;
import ru.mrbedrockpy.craftengine.server.network.packet.util.*;
import ru.mrbedrockpy.craftengine.server.world.entity.ServerPlayerEntity;

import java.util.Arrays;

import static ru.mrbedrockpy.craftengine.server.Server.MAX_PACKETS_PER_TICK;

public final class GameClient {
    private final ConcurrentQueue<Server.IncomingPacket> incomingQueue = new ConcurrentQueue<>();
    private NetworkManager network;

    private final PacketRegistry registry;
    private final ClientPacketHandler handler;
    private PlayerConnection connection;
    @Setter
    private GameProfile profile;

    public GameClient(PacketRegistry registry, ClientPacketHandler handler) {
        this.registry = registry;
        this.handler = handler;
    }

    public void connect(String host, int port) {
        network = NetworkManager.client(host, port, incomingQueue, registry);
        network.start();
        Channel ch = network.connectSync();
        connection = new PlayerConnection(PacketDirection.C2S, ch, registry);
        CraftEngineClient.INSTANCE.eventManager.callEvent(new ClientConnectEvent(connection, host, port));
        connection.send(new ClientLoginPacketC2S(profile.name()));
    }

    public void tick(){
        int processed = 0;
        while (processed < MAX_PACKETS_PER_TICK && incomingQueue.size() > 0) {
            Server.IncomingPacket in;
            try {
                in = incomingQueue.poll();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            if (in == null) break;

            Channel ch = in.channel();
            Packet  packet  = in.packet();

            try {
                ClientHandleContext ctx = new ClientHandleContext.Builder().client(CraftEngineClient.INSTANCE).channel(ch).sender(connection).build();
                handler.handle(ctx, packet);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            processed++;
        }
    }

    public void send(Packet packet) {
        if(connection != null && connection.isOpen()) connection.send(packet);
    }

    public void close() {
        if(network != null) network.shutdown();
    }
}