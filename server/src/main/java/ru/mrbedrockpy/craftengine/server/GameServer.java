package ru.mrbedrockpy.craftengine.server;

import ru.mrbedrockpy.craftengine.core.world.block.Blocks;
import ru.mrbedrockpy.craftengine.server.network.packet.*;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.BlockBreakPacketC2S;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.BlockUpdatePacketS2C;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.ClientLoginPacketC2S;
import ru.mrbedrockpy.craftengine.server.world.entity.ServerPlayerEntity;

public class GameServer {
    private static final ServerPacketHandler packetHandler = new ServerPacketHandler(PacketRegistry.INSTANCE);
    private static Server server;
    public static void main(String[] args) {

        int port = 25566;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("--port") && i + 1 < args.length) {
                port = Integer.parseInt(args[i + 1]);
            }
        }

        server = new CraftEngineServer(port, packetHandler);
        server.onInit();
        Packets.register();

        packetHandler.register(BlockBreakPacketC2S.class, (context, pkt) -> {
            for(ServerPlayerEntity player : server.getPlayers()) {
                player.send(new BlockUpdatePacketS2C(pkt.pos(), Blocks.AIR));
            }
        });

        packetHandler.register(ClientLoginPacketC2S.class, (context, packet) -> {
            context.server().onClientLogin(context, packet);
        });

        server.start();
    }
}