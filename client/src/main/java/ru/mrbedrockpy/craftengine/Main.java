package ru.mrbedrockpy.craftengine;

public class Main {

    public static void main(String[] args) {
        try {
            CraftEngineClient.INSTANCE.run();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}