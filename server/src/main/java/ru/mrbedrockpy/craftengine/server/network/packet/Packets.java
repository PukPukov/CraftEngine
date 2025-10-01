package ru.mrbedrockpy.craftengine.server.network.packet;

import ru.mrbedrockpy.craftengine.server.network.packet.custom.BlockBreakC2S;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.BlockUpdatePacketS2C;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.ClientLoginPacketC2S;

public class Packets {
    public static void register(){
        PacketRegistry.INSTANCE.register(PacketDirection.C2S, BlockBreakC2S.class, BlockBreakC2S.CODEC);
        PacketRegistry.INSTANCE.register(PacketDirection.S2C, BlockUpdatePacketS2C.class, BlockUpdatePacketS2C.CODEC);
        PacketRegistry.INSTANCE.register(PacketDirection.C2S, ClientLoginPacketC2S.class, ClientLoginPacketC2S.CODEC);
    }
}
