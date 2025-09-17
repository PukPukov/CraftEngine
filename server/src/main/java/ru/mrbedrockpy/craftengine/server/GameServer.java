package ru.mrbedrockpy.craftengine.server;

import ru.mrbedrockpy.craftengine.server.network.ConcurrentQueue;
import ru.mrbedrockpy.craftengine.server.network.NetworkManager;
import ru.mrbedrockpy.craftengine.server.network.packet.Packet;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketRegistry;
import ru.mrbedrockpy.craftengine.server.network.packet.TestPacket;

public class GameServer {

    private final ConcurrentQueue<Packet> incomingQueue = new ConcurrentQueue<>();
    private final NetworkManager networkManager;
    private final Logger logger = Logger.getLogger(GameServer.class);
    private volatile boolean running = true;
    
    public GameServer(int port) {
        this.networkManager = new NetworkManager(port, incomingQueue);
    }
    
    public void start() {
        networkManager.start();
        System.out.println("Starting game loop...");
        long lastUpdate = System.currentTimeMillis();
        while (running) {
            long currentTime = System.currentTimeMillis();
            float deltaTime = (currentTime - lastUpdate) / 1000.0f;
            lastUpdate = currentTime;
            try {
                processIncomingPackets();
                Thread.sleep(10);
            } catch (InterruptedException e) {
                System.out.println("Game loop interrupted");
                running = false;
            }
        }
        shutdown();
    }
    
    private void processIncomingPackets() throws InterruptedException {
        while (incomingQueue.size() > 0) {
            Packet packet = incomingQueue.poll();
            logger.info("New packet");
            if (packet instanceof TestPacket testPacket) logger.info("Packet with number: " + testPacket.getNum());
        }
    }
    
    public void shutdown() {
        running = false;
        networkManager.shutdown();
        logger.info("Game server shutdown complete");
    }
    
    public static void main(String[] args) {
        GameServer server = new GameServer(8080);
        server.start();
    }
}