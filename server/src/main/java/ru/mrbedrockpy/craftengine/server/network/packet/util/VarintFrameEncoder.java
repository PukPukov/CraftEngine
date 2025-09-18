package ru.mrbedrockpy.craftengine.server.network.packet.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

@ChannelHandler.Sharable
public final class VarintFrameEncoder extends MessageToMessageEncoder<ByteBuf> {
    @Override protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        ByteBuf outBuf = ctx.alloc().buffer(5 + msg.readableBytes());
        VarInt.write(outBuf, msg.readableBytes());
        outBuf.writeBytes(msg, msg.readerIndex(), msg.readableBytes());
        out.add(outBuf);
    }
}