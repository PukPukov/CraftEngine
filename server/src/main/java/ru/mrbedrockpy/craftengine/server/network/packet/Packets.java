package ru.mrbedrockpy.craftengine.server.network.packet;

import ru.mrbedrockpy.craftengine.server.network.packet.custom.*;

public class Packets {

    public static void register() {
        PacketRegistry.INSTANCE.register(BlockBreakPacketC2S.class);
        PacketRegistry.INSTANCE.register(ClientLoginPacketC2S.class);
        PacketRegistry.INSTANCE.register(ChatMessagePacketC2S.class);

        PacketRegistry.INSTANCE.register(BlockUpdatePacketS2C.class);
        PacketRegistry.INSTANCE.register(ChatMessagePacketS2C.class);
    }
}
