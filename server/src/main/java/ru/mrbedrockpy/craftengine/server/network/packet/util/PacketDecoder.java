package ru.mrbedrockpy.craftengine.server.network.packet.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodec;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketRegistry;
import ru.mrbedrockpy.craftengine.server.network.packet.Packet;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketDirection;

public final class PacketDecoder extends SimpleChannelInboundHandler<ByteBuf> {
    private final PacketRegistry registry;
    public PacketDecoder(PacketRegistry r) { this.registry = r; }

    @Override protected void channelRead0(ChannelHandlerContext ctx, ByteBuf frame) {
        int id = VarInt.read(frame);
        PacketCodec<? extends Packet> codec = registry.byId(PacketDirection.C2S, id);
        if (codec == null) throw new IllegalStateException("Unknown clientbound id: " + id);
        Packet pkt = codec.decode(frame);
        ctx.fireChannelRead(pkt);
    }
}