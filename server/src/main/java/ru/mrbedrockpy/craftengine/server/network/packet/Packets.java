package ru.mrbedrockpy.craftengine.server.network.packet;

import ru.mrbedrockpy.craftengine.server.network.packet.custom.BlockBreakPacketC2S;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.BlockUpdatePacketS2C;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.ClientLoginPacketC2S;

public class Packets {

    public static void register() {
        PacketRegistry.INSTANCE.register(BlockBreakPacketC2S.class);
        PacketRegistry.INSTANCE.register(ClientLoginPacketC2S.class);

        PacketRegistry.INSTANCE.register(BlockUpdatePacketS2C.class);
    }
}
