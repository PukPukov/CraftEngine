package ru.mrbedrockpy.craftengine.client;

import ru.mrbedrockpy.craftengine.client.network.auth.GameProfile;
import ru.mrbedrockpy.craftengine.core.util.config.CraftEngineConfig;

public class Main {

    public static void main(String[] args) {
        try {
            CraftEngineClient.INSTANCE.run();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}