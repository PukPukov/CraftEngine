package ru.mrbedrockpy.craftengine.server.network.codec;

import io.netty.buffer.ByteBuf;
import ru.mrbedrockpy.craftengine.server.network.packet.Packet;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface PacketCodec<P> {
    void encode(P pkt, ByteBuf out);
    P decode(ByteBuf in);

    static <P extends Packet> PacketCodec<P> of(
            BiConsumer<P, ByteBuf> encoder,
            Function<ByteBuf, P> decoder
    ) {
        return new PacketCodec<>() {
            @Override
            public void encode(P pkt, ByteBuf out) {
                encoder.accept(pkt, out);
            }

            @Override
            public P decode(ByteBuf in) {
                return decoder.apply(in);
            }
        };
    }
}