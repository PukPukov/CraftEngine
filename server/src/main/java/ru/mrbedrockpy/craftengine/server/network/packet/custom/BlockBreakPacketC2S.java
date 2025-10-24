package ru.mrbedrockpy.craftengine.server.network.packet.custom;

import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodec;
import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodecs;
import ru.mrbedrockpy.craftengine.server.network.packet.Packet;

public record BlockBreakPacketC2S(Vector3i pos) implements Packet {}
