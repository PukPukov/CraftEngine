package ru.mrbedrockpy.craftengine.server.network.packet.custom;

import ru.mrbedrockpy.craftengine.server.network.packet.Packet;

public record ClientLoginPacketC2S(String name) implements Packet {}
