package ru.mrbedrockpy.craftengine.server.network.packet.custom;

import ru.mrbedrockpy.craftengine.server.network.packet.Packet;

public record ChatMessagePacketC2S(String message) implements Packet {}