package ru.mrbedrockpy.craftengine.server.network.packet.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public final class VarintFrameDecoder extends ByteToMessageDecoder {
    @Override protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        in.markReaderIndex();
        if (!in.isReadable()) return;
        int len;
        try { len = VarInt.read(in); } catch (IndexOutOfBoundsException e) { in.resetReaderIndex(); return; }
        if (in.readableBytes() < len) { in.resetReaderIndex(); return; }
        out.add(in.readRetainedSlice(len));
    }
}