package ru.mrbedrockpy.craftengine.server.network.packet;

import io.netty.channel.Channel;
import ru.mrbedrockpy.craftengine.server.Logger;
import ru.mrbedrockpy.craftengine.server.Server;
import ru.mrbedrockpy.craftengine.server.world.entity.ServerPlayerEntity;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public final class PacketHandleContext {

    private final Channel channel;
    private final ServerPlayerEntity player;
    private final Server server;
    private final PacketSender sender;
    private final Executor serverExecutor;
    private final Logger logger;
    private final long tick;
    private final Instant now;

    private final Map<String, Object> attrs = new ConcurrentHashMap<>();

    private PacketHandleContext(Builder b) {
        this.channel = b.channel;
        this.player = b.player;
        this.server = b.server;
        this.sender = b.sender;
        this.serverExecutor = b.serverExecutor;
        this.logger = b.logger;
        this.tick = b.tick;
        this.now = b.now != null ? b.now : Instant.now();
    }

    // === базовый доступ ===

    public Channel channel() { return channel; }

    public ServerPlayerEntity player() { return player; }
    public Server server() { return server; }

    public UUID playerId() { return player != null ? player.getUuid() : null; }

    public Logger logger() { return logger; }

    public long tick() { return tick; }

    public Instant now() { return now; }

    public InetSocketAddress remoteAddress() {
        return (InetSocketAddress) channel.remoteAddress();
    }

    public void send(Packet pkt)        { sender.send(pkt); }

    public void sendNow(Packet pkt)     { sender.sendNow(pkt); }

    public void flush()                 { sender.flush(); }

    public void disconnect(String reason) { sender.close(reason); }

    public boolean isConnected() { return sender.isOpen(); }

    public void runOnServer(Runnable r) { serverExecutor.execute(r); }

    public PacketHandleContext setAttr(String key, Object val) {
        if (val == null) attrs.remove(key); else attrs.put(key, val);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttr(String key) { return (T) attrs.get(key); }

    public static final class Builder {
        private Channel channel;
        private ServerPlayerEntity player;
        private Server server;
        private PacketSender sender;
        private Executor serverExecutor;
        private Logger logger;
        private long tick;
        private Instant now;

        public Builder channel(Channel ch)          { this.channel = ch; return this; }
        public Builder player(ServerPlayerEntity p) { this.player = p; return this; }
        public Builder server(Server s) { this.server = s; return this; }
        public Builder sender(PacketSender s)       { this.sender = s; return this; }
        public Builder serverExecutor(Executor e)   { this.serverExecutor = e; return this; }
        public Builder logger(Logger l)             { this.logger = l; return this; }
        public Builder tick(long t)                 { this.tick = t; return this; }
        public Builder now(Instant n)               { this.now = n; return this; }

        public PacketHandleContext build() {
            if (channel == null || sender == null || serverExecutor == null || logger == null)
                throw new IllegalStateException("Missing required fields");
            return new PacketHandleContext(this);
        }
    }
}