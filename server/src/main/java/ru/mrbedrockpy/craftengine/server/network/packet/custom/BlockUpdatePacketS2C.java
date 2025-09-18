package ru.mrbedrockpy.craftengine.server.network.packet.custom;

import io.netty.buffer.ByteBuf;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.core.world.block.Block;
import ru.mrbedrockpy.craftengine.server.Server;
import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodec;
import ru.mrbedrockpy.craftengine.server.network.packet.Packet;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketHandleContext;
import ru.mrbedrockpy.craftengine.server.registry.Registries;
import ru.mrbedrockpy.craftengine.server.world.entity.ServerPlayerEntity;

public record BlockUpdatePacketS2C(Vector3i pos, Block block) implements Packet {
    public static final PacketCodec<BlockUpdatePacketS2C> CODEC = PacketCodec.of(
            BlockUpdatePacketS2C::encode,
            BlockUpdatePacketS2C::decode
    );

    @Override
    public void handle(PacketHandleContext ctx) {
    }

    private static void encode(BlockUpdatePacketS2C pkt, ByteBuf buf){
        BlockBreakC2S.CODEC.encode(new BlockBreakC2S(pkt.pos), buf);
        buf.writeInt(Registries.BLOCKS.getId(pkt.block));
    }

    private static BlockUpdatePacketS2C decode(ByteBuf buf){
        Vector3i pos = BlockBreakC2S.CODEC.decode(buf).pos();
        Block block = Registries.BLOCKS.get(buf.readInt());
        return new BlockUpdatePacketS2C(pos, block);
    }
}
