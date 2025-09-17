package ru.mrbedrockpy.craftengine.client.network.packet;

import org.joml.Vector3i;

public record BlockBreakC2S(byte b) implements Packet {
    public static final PacketCodec<BlockBreakC2S> CODEC = PacketCodec.of(
            (pkt, buf) -> buf.writeByte(pkt.b()),
            (buf) -> new BlockBreakC2S(buf.readByte())
    );
}
