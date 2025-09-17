package ru.mrbedrockpy.craftengine.server.network.packet;

import lombok.Getter;

@Getter
public abstract class Packet {

    private final long timestamp;
    
    public Packet() {
        this.timestamp = System.currentTimeMillis();
    }
}
