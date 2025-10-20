package ru.mrbedrockpy.craftengine.server.network.packet.custom;

import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodec;
import ru.mrbedrockpy.craftengine.server.network.packet.Packet;
import ru.mrbedrockpy.craftengine.server.network.packet.util.ByteBufUtil;

public record ClientLoginPacketC2S(String name) implements Packet {
    public static final PacketCodec<ClientLoginPacketC2S> CODEC = PacketCodec.of(
            (clientLoginPacketC2S, buf) -> ByteBufUtil.writeString(buf, clientLoginPacketC2S.name),
            buf -> new ClientLoginPacketC2S(ByteBufUtil.readString(buf))
    );
}
