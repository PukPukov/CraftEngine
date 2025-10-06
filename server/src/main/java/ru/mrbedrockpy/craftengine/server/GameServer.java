package ru.mrbedrockpy.craftengine.server;

import ru.mrbedrockpy.craftengine.core.world.block.Block;
import ru.mrbedrockpy.craftengine.core.world.block.Blocks;
import ru.mrbedrockpy.craftengine.server.network.packet.*;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.BlockBreakC2S;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.BlockUpdatePacketS2C;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.ClientLoginPacketC2S;
import ru.mrbedrockpy.craftengine.server.world.entity.ServerPlayerEntity;

import javax.swing.plaf.IconUIResource;

public class GameServer {
    private static final ServerPacketHandler packetHandler = new ServerPacketHandler(PacketRegistry.INSTANCE);
    private static Server server;
    public static void main(String[] args) {
        server = new CraftEngineServer(Integer.parseInt(args[0]), packetHandler);
        server.onInit();
        Packets.register();

        packetHandler.register(BlockBreakC2S.class, (context, pkt) -> {
            for(ServerPlayerEntity player : server.getPlayers()) {
                player.send(new BlockUpdatePacketS2C(pkt.pos(), Blocks.AIR));
            }
        });
        packetHandler.register(ClientLoginPacketC2S.class, (context, packet) -> context.server().onClientLogin(context, packet));

        server.start();
    }
}