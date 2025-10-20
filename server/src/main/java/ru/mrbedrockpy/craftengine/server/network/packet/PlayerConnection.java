package ru.mrbedrockpy.craftengine.server.network.packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodec;
import ru.mrbedrockpy.craftengine.server.network.packet.util.VarInt;

public final class PlayerConnection implements PacketSender {
    private final Channel channel;
    private final PacketRegistry registry;
    private final PacketDirection direction;

    public PlayerConnection(PacketDirection direction, Channel ch, PacketRegistry registry) {
        this.channel = ch;
        this.registry = registry;
        this.direction = direction;
    }

    @Override
    public void send(Packet packet) {
        if (!isOpen()) return;

        ByteBuf buf = channel.alloc().buffer();
        try {
            encodePacket(packet, buf);
            channel.writeAndFlush(buf);
        } catch (Exception ex) {
            buf.release();
            throw ex;
        }
    }

    @Override
    public void sendNow(Packet packet) {

    }

    @Override
    public void flush() {
        if (!isOpen()) return;
        channel.eventLoop().execute(channel::flush);
    }

    @Override
    public void close(String reason) {
        if (!isOpen()) return;
        channel.eventLoop().execute(channel::close);
    }

    @Override
    public boolean isOpen() {
        return channel != null && channel.isActive();
    }

    @SuppressWarnings("unchecked")
    private void encodePacket(Packet p, ByteBuf out) {
        int id = registry.idOf(direction, p.getClass());

        VarInt.write(out, id);

        PacketCodec<Packet> codec =
                (PacketCodec<Packet>) registry.byId(direction, id);
        codec.encode(p, out);
    }
}