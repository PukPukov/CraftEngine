package ru.mrbedrockpy.craftengine.client.network;


import ru.mrbedrockpy.craftengine.client.network.packet.Packet;
import ru.mrbedrockpy.craftengine.client.network.packet.PacketCodec;
import ru.mrbedrockpy.craftengine.client.network.packet.PacketDirection;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class PacketRegistry {
    private static final class Key {
        final PacketDirection dir; final Class<? extends Packet> type;
        Key(PacketDirection d, Class<? extends Packet> t) { dir=d; type=t; }
        public boolean equals(Object o){ if(this==o)return true; if(!(o instanceof Key k))return false; return dir==k.dir && type.equals(k.type); }
        public int hashCode(){ return Objects.hash(dir, type); }
    }

    private final Map<Integer, PacketCodec<? extends Packet>> idToCodecClient = new HashMap<>();
    private final Map<Integer, PacketCodec<? extends Packet>> idToCodecServer = new HashMap<>();
    private final Map<Key, Integer> classToId = new HashMap<>();

    public <P extends Packet> void register(PacketDirection dir, int id, Class<P> cls, PacketCodec<P> codec) {
        if (dir == PacketDirection.C2S) idToCodecClient.put(id, codec);
        else                               idToCodecServer.put(id, codec);
        classToId.put(new Key(dir, cls), id);
    }

    public PacketCodec<? extends Packet> byId(PacketDirection dir, int id) {
        return (dir == PacketDirection.C2S) ? idToCodecClient.get(id) : idToCodecServer.get(id);
    }

    public int idOf(PacketDirection dir, Class<? extends Packet> cls) {
        Integer id = classToId.get(new Key(dir, cls));
        if (id == null) throw new IllegalStateException("Unregistered packet: " + dir + " " + cls.getName());
        return id;
    }
}