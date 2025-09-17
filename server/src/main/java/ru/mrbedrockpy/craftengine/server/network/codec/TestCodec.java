package ru.mrbedrockpy.craftengine.server.network.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import ru.mrbedrockpy.craftengine.server.network.packet.TestPacket;

public class TestCodec extends PacketCodec<TestPacket> {

    public TestCodec(byte id) {
        super(id, TestPacket.class);
    }

    @Override
    public TestPacket decode(ByteBuf data) {
        return new TestPacket(data.readInt());
    }

    @Override
    public ByteBuf encode(TestPacket packet) {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(getId());
        buf.writeInt(packet.getNum());
        return buf;
    }
}
