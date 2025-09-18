package ru.mrbedrockpy.craftengine.server.network.packet;

import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodec;

public record BlockBreakC2S(Vector3i pos) implements Packet {
    public static final PacketCodec<BlockBreakC2S> CODEC = PacketCodec.of(
            (pkt, buf) -> {
                buf.writeInt(pkt.pos.x);
                buf.writeInt(pkt.pos.y);
                buf.writeInt(pkt.pos.z);
            },
            (buf) -> new BlockBreakC2S(new Vector3i(buf.readInt(), buf.readInt(), buf.readInt()))
    );
}
