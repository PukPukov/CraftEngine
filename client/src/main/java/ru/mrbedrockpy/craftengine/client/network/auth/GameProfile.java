package ru.mrbedrockpy.craftengine.client.network.auth;

public record GameProfile(String name) {
    public static GameProfile debug() {
        return new GameProfile("Dev1");
    }
}
