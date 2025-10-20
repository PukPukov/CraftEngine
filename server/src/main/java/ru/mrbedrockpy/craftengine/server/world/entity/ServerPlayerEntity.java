package ru.mrbedrockpy.craftengine.server.world.entity;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.core.world.World;
import ru.mrbedrockpy.craftengine.core.world.entity.PlayerEntity;
import ru.mrbedrockpy.craftengine.server.network.packet.Packet;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketSender;

public class ServerPlayerEntity extends PlayerEntity {

    @Getter
    private final UUID uuid;
    @Getter
    private final String name;
    private final PacketSender connection;

    @Setter
    @Getter
    private int latencyMs;

    public ServerPlayerEntity(UUID uuid, String name,
                              Vector3f spawnPos, World world,
                              PacketSender connection) {
        super(spawnPos, world);
        this.uuid = uuid;
        this.name = name;
        this.connection = connection;
    }

    public void send(Packet pkt) {
        if (connection != null) connection.send(pkt);
    }

    public void sendNow(Packet pkt) {
        if (connection != null) connection.sendNow(pkt);
    }

    public void flush() {
        if (connection != null) connection.flush();
    }

    public void disconnect(String reason) {
        if (connection != null) connection.close(reason);
    }

    public boolean isConnected() {
        return connection != null && connection.isOpen();
    }
}
