package ru.mrbedrockpy.craftengine.server;

import ru.mrbedrockpy.craftengine.server.network.ConcurrentQueue;
import ru.mrbedrockpy.craftengine.server.network.NetworkManager;
import ru.mrbedrockpy.craftengine.server.network.packet.BlockBreakC2S;
import ru.mrbedrockpy.craftengine.server.network.packet.Packet;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketDirection;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketRegistry;
import ru.mrbedrockpy.craftengine.server.world.TickSystem;

public class GameServer {

    private final ConcurrentQueue<Packet> incomingQueue = new ConcurrentQueue<>();
    private final NetworkManager networkManager;
    private final Logger logger = Logger.getLogger(GameServer.class);
    private volatile boolean running = true;

    private final PacketRegistry packetRegistry = new PacketRegistry();
    private final TickSystem tickSystem = new TickSystem(20);

    private static final int MAX_PACKETS_PER_TICK = 500;

    public GameServer(int port) {
        this.networkManager = new NetworkManager(port, incomingQueue, packetRegistry);
    }

    public void start() {
        networkManager.start();
        logger.info("Network started");

        packetRegistry.register(PacketDirection.C2S, 0, BlockBreakC2S.class, BlockBreakC2S.CODEC);

        tickSystem.addListener(this::tickPackets);

        waitLoop();
        shutdown();
    }

    private void waitLoop() {
        long prevTime = System.currentTimeMillis();
        while (running) {
            long now = System.currentTimeMillis();
            double deltaTime = (double) (now - prevTime) / 1000;
            prevTime = now;
            try {
                Thread.sleep(10);
                tickSystem.update(deltaTime);
            }
            catch (InterruptedException e) { break; }
        }
    }

    private void tickPackets() {
        int processed = 0;
        while (processed < MAX_PACKETS_PER_TICK && incomingQueue.size() > 0) {
            Packet packet = null;
            try {
                packet = incomingQueue.poll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (packet == null) break;
            try {
                handlePacket(packet);
            } catch (Exception ex) {
                logger.info("Packet handling error");
            }
            processed++;
        }
        if (incomingQueue.size() > 0) {

        }
    }

    private void handlePacket(Packet p) {
        logger.info("New packet: " + p.toString());
    }

    public void requestStop() {
        running = false;
    }

    public void shutdown() {
        networkManager.shutdown();
        logger.info("Game server shutdown complete");
    }

    public static void main(String[] args) {
        GameServer server = new GameServer(8080);
        server.start();
    }
}