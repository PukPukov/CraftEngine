package ru.mrbedrockpy.craftengine.client;

public class Main {

    public static void main(String[] args) {
        try {
            CraftEngineClient.INSTANCE.run();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}