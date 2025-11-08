package ru.mrbedrockpy.craftengine.server.network.packet.custom;

import ru.mrbedrockpy.craftengine.server.network.packet.Packet;

import java.util.UUID;

public record ChatMessagePacketS2C(String name, String message) implements Packet {}
