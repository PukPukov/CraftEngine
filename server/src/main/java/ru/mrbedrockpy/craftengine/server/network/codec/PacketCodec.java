package ru.mrbedrockpy.craftengine.server.network.codec;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.mrbedrockpy.craftengine.server.network.packet.Packet;

@Getter
@AllArgsConstructor
public abstract class PacketCodec<P extends Packet> {

    private final byte id;
    private final Class<P> packetClass;

    public abstract P decode(ByteBuf data);
    public abstract ByteBuf encode(P packet);

}
