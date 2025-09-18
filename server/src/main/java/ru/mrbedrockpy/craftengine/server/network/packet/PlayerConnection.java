package ru.mrbedrockpy.craftengine.server.network.packet;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodec;
import ru.mrbedrockpy.craftengine.server.network.packet.util.VarInt;

public final class PlayerConnection implements PacketSender {
    private final Channel ch;
    private final PacketRegistry registry;

    public PlayerConnection(Channel ch, PacketRegistry registry) {
        this.ch = ch;
        this.registry = registry;
    }

    @Override
    public void send(Packet packet) {
        if (!isOpen()) return;
        ch.eventLoop().execute(() -> {
            ByteBuf buf = null;
            try {
                buf = ch.alloc().ioBuffer();
                encodePacket(packet, buf);
                ch.write(buf);
                buf = null;
            } catch (Throwable t) {
                if (buf != null) buf.release();
            }
        });
    }

    @Override
    public void sendNow(Packet packet) {
        if (!isOpen()) return;
        ch.eventLoop().execute(() -> {
            ByteBuf buf = null;
            try {
                buf = ch.alloc().ioBuffer();
                encodePacket(packet, buf);
                ch.writeAndFlush(buf);
                buf = null;
            } catch (Throwable t) {
                if (buf != null) buf.release();
            }
        });
    }

    @Override
    public void flush() {
        if (!isOpen()) return;
        ch.eventLoop().execute(ch::flush);
    }

    @Override
    public void close(String reason) {
        if (!isOpen()) return;
        ch.eventLoop().execute(() -> ch.close());
    }

    @Override
    public boolean isOpen() {
        return ch != null && ch.isActive();
    }

    @SuppressWarnings("unchecked")
    private void encodePacket(Packet p, ByteBuf out) {
        int id = registry.idOf(PacketDirection.S2C, p.getClass());

        VarInt.write(out, id);

        PacketCodec<Packet> codec =
                (PacketCodec<Packet>) registry.byId(PacketDirection.S2C, id);
        codec.encode(p, out);
    }
}