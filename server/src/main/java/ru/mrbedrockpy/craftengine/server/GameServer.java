package ru.mrbedrockpy.craftengine.server;

public class GameServer {
    private static final Server server = new CraftEngineServer(8080);
    public static void main(String[] args) {
        server.onInit();
        server.start();
    }
}