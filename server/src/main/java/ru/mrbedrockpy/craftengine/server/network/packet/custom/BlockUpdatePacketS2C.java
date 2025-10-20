package ru.mrbedrockpy.craftengine.server.network.packet.custom;

import io.netty.buffer.ByteBuf;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.core.world.block.Block;
import ru.mrbedrockpy.craftengine.server.Server;
import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodec;
import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodecs;
import ru.mrbedrockpy.craftengine.server.network.packet.Packet;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketHandleContext;
import ru.mrbedrockpy.craftengine.server.world.entity.ServerPlayerEntity;

public record BlockUpdatePacketS2C(Vector3i pos, Block block) implements Packet {
    public static final PacketCodec<BlockUpdatePacketS2C> CODEC = PacketCodec.of(
            BlockUpdatePacketS2C::encode,
            BlockUpdatePacketS2C::decode
    );

    private static void encode(BlockUpdatePacketS2C pkt, ByteBuf buf){
        PacketCodecs.POS_CODEC.encode(pkt.pos, buf);
        PacketCodecs.BLOCK_CODEC.encode(pkt.block, buf);
    }

    private static BlockUpdatePacketS2C decode(ByteBuf buf){
        Vector3i pos = PacketCodecs.POS_CODEC.decode(buf);
        Block block = PacketCodecs.BLOCK_CODEC.decode(buf);
        return new BlockUpdatePacketS2C(pos, block);
    }
}
