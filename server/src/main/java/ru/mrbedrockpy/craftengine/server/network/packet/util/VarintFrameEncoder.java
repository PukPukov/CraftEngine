package ru.mrbedrockpy.craftengine.server.network.packet.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public final class VarintFrameEncoder extends MessageToMessageEncoder<ByteBuf> {
    private static void writeVarInt(ByteBuf out, int value) {
        while ((value & ~0x7F) != 0) {
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte(value);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        ByteBuf buf = ctx.alloc().buffer(5 + msg.readableBytes());
        writeVarInt(buf, msg.readableBytes());
        buf.writeBytes(msg, msg.readerIndex(), msg.readableBytes());
        out.add(buf);
    }
}