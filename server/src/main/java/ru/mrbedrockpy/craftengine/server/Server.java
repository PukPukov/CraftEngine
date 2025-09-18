package ru.mrbedrockpy.craftengine.server;


import io.netty.channel.*;
import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.server.network.ConcurrentQueue;
import ru.mrbedrockpy.craftengine.server.network.NetworkManager;
import ru.mrbedrockpy.craftengine.server.network.packet.*;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.BlockBreakC2S;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.BlockUpdatePacketS2C;
import ru.mrbedrockpy.craftengine.server.world.TickSystem;
import ru.mrbedrockpy.craftengine.server.world.World;
import ru.mrbedrockpy.craftengine.server.world.entity.ServerPlayerEntity;
import ru.mrbedrockpy.craftengine.server.world.generator.PerlinChunkGenerator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Server {

    protected final Logger logger = Logger.getLogger(getClass());
    protected final ConcurrentQueue<IncomingPacket> incomingQueue = new ConcurrentQueue<>();
    protected final PacketRegistry packetRegistry = new PacketRegistry();

    protected final TickSystem tickSystem = new TickSystem(20);
    protected volatile boolean running = true;

    protected final Map<UUID, ServerPlayerEntity> playersById = new ConcurrentHashMap<>();
    protected final Map<ChannelId, ServerPlayerEntity> playersByChannel = new ConcurrentHashMap<>();

    private static final int MAX_PACKETS_PER_TICK = 500;
    private NetworkManager network;
    public final World world = new World(100, new PerlinChunkGenerator(0l, 5, 5, 5));

    protected Server() {
    }

    public void start() {
        onInit();

        registerPackets(packetRegistry);

        this.network = createNetworkManager(incomingQueue, packetRegistry);
        if (network != null) {
            network.start();
            network.addListener(new NetworkManager.ConnectionListener() {
                @Override
                public void onConnected(Channel ch) {
                    handleConnect(ch);
                }

                @Override
                public void onDisconnected(Channel ch) {
                    handleDisconnect(ch);
                }
            });
            logger.info("Network started");
        }

        tickSystem.addListener(() -> {
            try { tickPackets(); } catch (Exception e) { logger.error("tickPackets error" + e); }
            try { onTick(); }     catch (Exception e) { logger.error("onTick error" + e); }
        });

        onStarted();

        waitLoop();

        shutdown();
    }

    public final void requestStop() { running = false; }

    public void shutdown() {
        try { onStopping(); } catch (Exception e) { logger.error("onStopping error" + e); }
        try {
            if (network != null) network.shutdown();
        } catch (Exception e) {
            logger.error("Network shutdown error" + e);
        }
        logger.info("Game server shutdown complete");
    }

    protected void onInit() {}

    protected void registerPackets(PacketRegistry reg) {
        reg.register(PacketDirection.C2S, 0, BlockBreakC2S.class, BlockBreakC2S.CODEC);
        reg.register(PacketDirection.S2C, 1, BlockUpdatePacketS2C.class, BlockUpdatePacketS2C.CODEC);
    }

    protected abstract NetworkManager createNetworkManager(ConcurrentQueue<IncomingPacket> in, PacketRegistry reg);

    protected void onTick() {
        world.tick();
    }

    protected void onStarted() {}

    protected void onStopping() {}

    protected void waitLoop() {
        long prevTime = System.currentTimeMillis();
        while (running) {
            long now = System.currentTimeMillis();
            double dt = (now - prevTime) / 1000.0;
            prevTime = now;
            try {
                tickSystem.update(dt);
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Throwable t) {
                logger.error("Main loop error " + t);
            }
        }
    }

    protected void tickPackets() {
        int processed = 0;
        while (processed < MAX_PACKETS_PER_TICK && incomingQueue.size() > 0) {
            IncomingPacket in;
            try {
                in = incomingQueue.poll();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            if (in == null) break;

            Channel ch = in.channel();
            Packet  p  = in.packet();

            try {
                ServerPlayerEntity player = playersByChannel.get(ch.id());

                PacketSender sender = new PlayerConnection(ch, packetRegistry);

                PacketHandleContext ctx = new PacketHandleContext.Builder()
                        .channel(ch)
                        .player(player)
                        .server(this)
                        .sender(sender)
                        .serverExecutor(Runnable::run)
                        .logger(logger)
                        .tick(0L)
                        .build();

                p.handle(ctx);
            } catch (Exception ex) {
                logger.error("Packet handling error: " + p + " " + ex);
            }
            processed++;
        }
    }

    protected void handleConnect(Channel ch) {
        PacketSender sender = new PlayerConnection(ch, packetRegistry);

        UUID uuid = UUID.randomUUID();
        String name = "Player_" + uuid.toString().substring(0, 8);

        ServerPlayerEntity player = new ServerPlayerEntity(
                uuid, name, new Vector3f(0, 64, 0), world, sender);

        playersById.put(uuid, player);
        playersByChannel.put(ch.id(), player);
    }

    protected void handleDisconnect(Channel ch) {
        ServerPlayerEntity p = playersByChannel.remove(ch.id());
        if (p != null) {
            playersById.remove(p.getUuid());
        }
    }

    public List<ServerPlayerEntity> getPlayers() {
        return (List<ServerPlayerEntity>) playersById.values();
    }

    public record IncomingPacket(Channel channel, Packet packet) {}
}
