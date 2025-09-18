package ru.mrbedrockpy.craftengine.server.network.packet.custom;

import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.server.Server;
import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodec;
import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodecs;
import ru.mrbedrockpy.craftengine.server.network.packet.Packet;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketHandleContext;
import ru.mrbedrockpy.craftengine.server.world.block.Blocks;
import ru.mrbedrockpy.craftengine.server.world.entity.ServerPlayerEntity;

public record BlockBreakC2S(Vector3i pos) implements Packet {
    public static final PacketCodec<BlockBreakC2S> CODEC = PacketCodec.of(
            (pkt, buf) -> PacketCodecs.POS_CODEC.encode(pkt.pos, buf),
            (buf) -> new BlockBreakC2S(PacketCodecs.POS_CODEC.decode(buf))
    );

    @Override
    public void handle(PacketHandleContext ctx) {
        ctx.player().getWorld().setBlock(pos, Blocks.AIR);
        Server server = ctx.server();
        for(ServerPlayerEntity player : server.getPlayers()){
            player.send(new BlockUpdatePacketS2C(pos, Blocks.AIR));
        }
    }
}
