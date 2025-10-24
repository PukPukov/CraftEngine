package ru.mrbedrockpy.craftengine.server.network.packet.custom;

import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodec;
import ru.mrbedrockpy.craftengine.server.network.packet.Packet;
import ru.mrbedrockpy.craftengine.server.network.packet.util.ByteBufUtil;

public record ClientLoginPacketC2S(String name) implements Packet {}
