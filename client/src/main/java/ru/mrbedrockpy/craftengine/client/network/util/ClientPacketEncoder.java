package ru.mrbedrockpy.craftengine.client.network.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import ru.mrbedrockpy.craftengine.client.network.PacketRegistry;
import ru.mrbedrockpy.craftengine.client.network.packet.Packet;
import ru.mrbedrockpy.craftengine.client.network.packet.PacketCodec;
import ru.mrbedrockpy.craftengine.client.network.packet.PacketDirection;

import java.util.List;

public final class ClientPacketEncoder extends MessageToMessageEncoder<Packet> {
    private final PacketRegistry reg;
    public ClientPacketEncoder(PacketRegistry r) { this.reg = r; }

    @Override protected void encode(ChannelHandlerContext ctx, Packet msg, List<Object> out) {
        int id = reg.idOf(PacketDirection.C2S, msg.getClass());
        ByteBuf body = ctx.alloc().buffer();
        VarInt.write(body, id);
        @SuppressWarnings("unchecked")
        PacketCodec<Packet> codec = (PacketCodec<Packet>) reg.byId(PacketDirection.C2S, id);
        codec.encode(msg, body);
        out.add(body);
    }
}