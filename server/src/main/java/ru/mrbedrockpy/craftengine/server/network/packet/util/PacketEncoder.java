package ru.mrbedrockpy.craftengine.server.network.packet.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodec;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketRegistry;
import ru.mrbedrockpy.craftengine.server.network.packet.Packet;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketDirection;

import java.util.List;

public final class PacketEncoder extends MessageToMessageEncoder<Packet> {
    private final PacketRegistry reg;
    private final PacketDirection dir;

    public PacketEncoder(PacketRegistry r, PacketDirection dir) {
        this.reg = r;
        this.dir = dir;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, List<Object> out) {
        int id = reg.idOf(dir, msg.getClass());
        ByteBuf body = ctx.alloc().buffer();
        VarInt.write(body, id);
        @SuppressWarnings("unchecked")
        PacketCodec<Packet> codec = (PacketCodec<Packet>) reg.byId(dir, id);
        codec.encode(msg, body);
        out.add(body);
    }
}