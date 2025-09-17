package ru.mrbedrockpy.craftengine.server.network.packet;

import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodec;
import ru.mrbedrockpy.craftengine.server.network.codec.TestCodec;

import java.util.HashMap;
import java.util.Map;

public class PacketRegistry {

    public static final PacketRegistry INSTANCE = new PacketRegistry();

    private final Map<Byte, PacketCodec<? extends Packet>> codecs = new HashMap<>();

    public PacketRegistry() {
        register((byte) 0, new TestCodec((byte) 0));
    }

    public void register(byte id, PacketCodec<?> codec) {
        if (!codecs.containsKey(id)) codecs.put(id, codec);
    }

    public PacketCodec<? extends Packet> getByPacket(Class<? extends Packet> packetClass) {
        for (PacketCodec<?> codec : codecs.values()) {
            if (codec.getPacketClass().equals(packetClass)) return codec;
        }
        return null;
    }

    public PacketCodec<? extends Packet> getById(byte id) {
        try {
            return codecs.get(id);
        } catch (NullPointerException e) {
            return null;
        }
    }
}
