package ru.mrbedrockpy.craftengine.server.network.packet;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TestPacket extends Packet {

    private final int num;

}
