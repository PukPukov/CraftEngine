package ru.mrbedrockpy.craftengine.server.network.packet;

import ru.mrbedrockpy.craftengine.server.CraftEngineServer;
import ru.mrbedrockpy.craftengine.server.Server;
import ru.mrbedrockpy.craftengine.server.world.entity.ServerPlayerEntity;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;

public final class ServerHandleContext extends PacketHandleContext {

    private final Server server;
    private final ServerPlayerEntity player;

    private ServerHandleContext(Builder b) {
        super(b);
        this.server = Objects.requireNonNull(b.server, "server");
        this.player = b.player;
    }

    public Server server() {
        return server;
    }

    public ServerPlayerEntity player() {
        return player;
    }

    public UUID playerId() {
        return player != null ? player.getUuid() : null;
    }

    @Override
    public boolean isServerSide() {
        return true;
    }

    public static final class Builder extends PacketHandleContext.Builder<Builder> {
        private Server server;
        private ServerPlayerEntity player;
        private Executor serverExecutor;

        public Builder server(Server s) {
            this.server = s;
            return this;
        }

        public Builder player(ServerPlayerEntity p) {
            this.player = p;
            return this;
        }

        public Builder serverExecutor(Executor e) {
            this.serverExecutor = e;
            return this;
        }

        @Override
        protected Builder self() {
            return this;
        }

        public ServerHandleContext build() {
            return new ServerHandleContext(this);
        }
    }
}