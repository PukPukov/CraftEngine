package ru.mrbedrockpy.craftengine.client.network.aut;

public record GameProfile(String name) {
    public static GameProfile debug() {
        return new GameProfile("Dev");
    }
}
