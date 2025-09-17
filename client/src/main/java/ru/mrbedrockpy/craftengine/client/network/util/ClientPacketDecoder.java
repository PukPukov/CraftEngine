package ru.mrbedrockpy.craftengine.client.network.util;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import ru.mrbedrockpy.craftengine.client.network.PacketRegistry;
import ru.mrbedrockpy.craftengine.client.network.packet.Packet;
import ru.mrbedrockpy.craftengine.client.network.packet.PacketCodec;
import ru.mrbedrockpy.craftengine.client.network.packet.PacketDirection;

public final class ClientPacketDecoder extends SimpleChannelInboundHandler<ByteBuf> {
    private final PacketRegistry registry;
    public ClientPacketDecoder(PacketRegistry r) { this.registry = r; }

    @Override protected void channelRead0(ChannelHandlerContext ctx, ByteBuf frame) {
        int id = VarInt.read(frame);
        PacketCodec<? extends Packet> codec = registry.byId(PacketDirection.C2S, id);
        if (codec == null) throw new IllegalStateException("Unknown clientbound id: " + id);
        Packet pkt = codec.decode(frame);
        ctx.fireChannelRead(pkt); // передаём дальше в SimpleChannelInboundHandler<Packet>
    }
}