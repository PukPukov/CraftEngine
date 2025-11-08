package ru.mrbedrockpy.craftengine.client.network;

import lombok.Getter;
import ru.mrbedrockpy.craftengine.client.CraftEngineClient;
import ru.mrbedrockpy.craftengine.client.world.entity.ClientPlayerEntity;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketHandleContext;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketSender;

import java.util.UUID;

public final class ClientHandleContext extends PacketHandleContext {

    @Getter
    private final CraftEngineClient client;
    @Getter
    private final UUID localPlayerId;
    private ClientHandleContext(Builder b) {
        super(b);
        this.client = b.clientExecutor;
        this.localPlayerId  = b.localPlayerId;
    }

    @Override public boolean isServerSide() { return false; }

    public static final class Builder extends PacketHandleContext.Builder<Builder> {
        private CraftEngineClient clientExecutor;
        private UUID localPlayerId;
        public Builder client(CraftEngineClient e) { this.clientExecutor = e; return this; }
        public Builder localPlayerId(UUID id)     { this.localPlayerId  = id; return this; }

        @Override protected Builder self()        { return this; }
        public ClientHandleContext build()        { return new ClientHandleContext(this); }
        public ClientPlayerEntity clientPlayer() { return clientExecutor.getPlayer(); }
    }
}