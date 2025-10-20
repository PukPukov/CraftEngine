package ru.mrbedrockpy.craftengine.client;

import ru.mrbedrockpy.craftengine.client.network.auth.GameProfile;

public class Main {

    public static void main(String[] args) {
        try {
            CraftEngineClient.INSTANCE.run(GameProfile.debug());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}