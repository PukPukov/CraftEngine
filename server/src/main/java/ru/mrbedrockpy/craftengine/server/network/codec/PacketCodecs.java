package ru.mrbedrockpy.craftengine.server.network.codec;

import org.joml.Vector3i;

public class PacketCodecs {
    public static final PacketCodec<Vector3i> POS_CODEC = PacketCodec.of(
            (pos, buf) -> {
                buf.writeInt(pos.x);
                buf.writeInt(pos.y);
                buf.writeInt(pos.z);
            },
            buf -> new Vector3i(buf.readInt(), buf.readInt(), buf.readInt())
    );
}
