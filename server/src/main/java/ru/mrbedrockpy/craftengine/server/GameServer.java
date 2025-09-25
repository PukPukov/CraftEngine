package ru.mrbedrockpy.craftengine.server;

import ru.mrbedrockpy.craftengine.core.world.block.Block;
import ru.mrbedrockpy.craftengine.server.network.packet.Packet;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketDirection;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketRegistry;
import ru.mrbedrockpy.craftengine.server.network.packet.ServerPacketHandler;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.BlockBreakC2S;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.BlockUpdatePacketS2C;
import ru.mrbedrockpy.craftengine.server.world.block.Blocks;
import ru.mrbedrockpy.craftengine.server.world.entity.ServerPlayerEntity;

import javax.swing.plaf.IconUIResource;

public class GameServer {
    private static final ServerPacketHandler packetHandler = new ServerPacketHandler(PacketRegistry.INSTANCE);
    private static final Server server = new CraftEngineServer(8080, packetHandler);
    public static void main(String[] args) {
        server.onInit();

        PacketRegistry.INSTANCE.register(PacketDirection.C2S, BlockBreakC2S.class, BlockBreakC2S.CODEC);
        PacketRegistry.INSTANCE.register(PacketDirection.S2C, BlockUpdatePacketS2C.class, BlockUpdatePacketS2C.CODEC);

        packetHandler.register(BlockBreakC2S.class, (context, packet) -> {
            if(packet instanceof BlockBreakC2S pkt) {
                System.out.println(pkt);
                for(ServerPlayerEntity player : server.getPlayers()) {
                    player.send(new BlockUpdatePacketS2C(pkt.pos(), Blocks.AIR));
                }
            }
        });

        server.start();
    }
}