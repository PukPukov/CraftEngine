package ru.mrbedrockpy.craftengine.server;


import io.netty.channel.*;
import lombok.RequiredArgsConstructor;
import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.core.world.World;
import ru.mrbedrockpy.craftengine.core.world.generator.PerlinChunkGenerator;
import ru.mrbedrockpy.craftengine.server.network.ConcurrentQueue;
import ru.mrbedrockpy.craftengine.server.network.NetworkManager;
import ru.mrbedrockpy.craftengine.server.network.packet.*;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.BlockBreakC2S;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.BlockUpdatePacketS2C;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.ClientLoginPacketC2S;
import ru.mrbedrockpy.craftengine.server.world.TickSystem;
import ru.mrbedrockpy.craftengine.server.world.entity.ServerPlayerEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class Server {

    protected final ServerPacketHandler handler;
    protected final Logger logger = Logger.getLogger(getClass());
    protected final ConcurrentQueue<IncomingPacket> incomingQueue = new ConcurrentQueue<>();
    protected final PacketRegistry packetRegistry = PacketRegistry.INSTANCE;

    protected final TickSystem tickSystem = new TickSystem(20);
    protected volatile boolean running = true;

    protected final Map<UUID, ServerPlayerEntity> playersById = new ConcurrentHashMap<>();
    protected final Map<ChannelId, ServerPlayerEntity> playersByChannel = new ConcurrentHashMap<>();
    protected final Set<ChannelId> awaitingLogin = ConcurrentHashMap.newKeySet();
    protected final Set<String> takenNames = ConcurrentHashMap.newKeySet();

    public static final int MAX_PACKETS_PER_TICK = 500;
    private NetworkManager network;
    public final World world = new World(100, PerlinChunkGenerator.DEFAULT);

    public void start() {
        onInit();

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

    protected void onInit() {
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
            Packet  packet  = in.packet();

            try {
                ServerPlayerEntity player = playersByChannel.get(ch.id());

                PacketSender sender = new PlayerConnection(PacketDirection.S2C, ch, packetRegistry);

                ServerHandleContext ctx = new ServerHandleContext.Builder()
                        .channel(ch)
                        .player(player)
                        .server(this)
                        .sender(sender)
                        .serverExecutor(Runnable::run)
                        .logger(logger)
                        .tick(0L)
                        .build();

                handler.handle(ctx, packet);
            } catch (Exception ex) {
                logger.error("Packet handling error: " + packet + " " + ex);
            }
            processed++;
        }
    }

    protected void handleConnect(Channel ch) {
        awaitingLogin.add(ch.id());
        logger.info("Channel " + ch.id() + " connected; awaiting login");
    }

    protected void handleDisconnect(Channel ch) {
        ServerPlayerEntity p = playersByChannel.get(ch.id());
        playersByChannel.remove(ch.id());
        boolean removedAwait = awaitingLogin.remove(ch.id());

        if (p != null) {
            playersById.remove(p.getUuid());
            takenNames.remove(p.getName().toLowerCase(Locale.ROOT)); // <—
            logger.info(p.getName() + " left");
        } else {
            logger.info("Channel " + ch.id() + " disconnected (no player), awaitingLogin.remove=" + removedAwait);
        }
    }

    public void onClientLogin(ServerHandleContext ctx, ClientLoginPacketC2S pkt) {
        Channel ch = ctx.channel();

        if (!awaitingLogin.remove(ch.id())) {
            logger.warn("Login received but channel already logged in: " + ch.id());
            return;
        }

        String rawName = pkt.name();
        String name = sanitizeName(rawName);
        if (name == null) {
            logger.warn("Bad player name from " + ch.id() + ": '" + rawName + "'");
            ch.close();
            return;
        }

        String key = name.toLowerCase(Locale.ROOT);
        boolean unique = takenNames.add(key);
        if (!unique) {
            logger.warn("Duplicate name: " + name + " from " + ch.id());
            ch.close();
            return;
        }

        UUID uuid = UUID.randomUUID();
        PacketSender sender = new PlayerConnection(PacketDirection.S2C, ch, packetRegistry);

        ServerPlayerEntity player = new ServerPlayerEntity(
                uuid, name, new Vector3f(0, 64, 0), world, sender);

        playersById.put(uuid, player);
        playersByChannel.put(ch.id(), player);

        logger.info(name + " joined (uuid=" + uuid + ")");
    }

    private static String sanitizeName(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.length() < 3 || s.length() > 16) return null;
        if (!s.matches("[A-Za-z0-9_]+")) return null;
        return s;
    }

    public List<ServerPlayerEntity> getPlayers() {
        return playersById.values().stream().toList();
    }

    public record IncomingPacket(Channel channel, Packet packet) {}
}
