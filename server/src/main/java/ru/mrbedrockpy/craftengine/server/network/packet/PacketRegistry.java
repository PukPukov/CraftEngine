package ru.mrbedrockpy.craftengine.server.network.packet;


import ru.mrbedrockpy.craftengine.server.network.codec.PacketCodec;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class PacketRegistry {
    public static final PacketRegistry INSTANCE = new PacketRegistry();

    private static final class Key {
        final PacketDirection dir;
        final Class<? extends Packet> type;

        Key(PacketDirection d, Class<? extends Packet> t) {
            dir = d;
            type = t;
        }

        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key k)) return false;
            return dir == k.dir && type.equals(k.type);
        }

        public int hashCode() {
            return Objects.hash(dir, type);
        }
    }

    private final Map<Integer, PacketCodec<? extends Packet>> idToCodecClient = new HashMap<>();
    private final Map<Integer, PacketCodec<? extends Packet>> idToCodecServer = new HashMap<>();
    private final Map<Key, Integer> classToId = new HashMap<>();
    private int currentId = 0;

    public <P extends Packet> void register(PacketDirection dir, Class<P> cls, PacketCodec<P> codec) {
        if (dir == PacketDirection.C2S) idToCodecClient.put(currentId, codec);
        else idToCodecServer.put(currentId, codec);
        classToId.put(new Key(dir, cls), currentId);
        currentId++;
    }

    public <P extends Packet> void register(PacketDirection dir, Class<P> cls) {
        PacketCodec<P> codec = PacketCodec.forRecord(cls);
        if (dir == PacketDirection.C2S) idToCodecClient.put(currentId, codec);
        else idToCodecServer.put(currentId, codec);
        classToId.put(new Key(dir, cls), currentId);
        currentId++;
    }

    public <P extends Packet> void register(Class<P> cls) {
        String name = cls.getSimpleName().toUpperCase(Locale.ROOT);

        PacketDirection dir;
        if (name.endsWith("C2S")) {
            dir = PacketDirection.C2S;
        } else if (name.endsWith("S2C")) {
            dir = PacketDirection.S2C;
        } else {
            throw new IllegalArgumentException(
                    "Invalid packet name: " + cls.getName() +
                            " — must end with C2S or S2C"
            );
        }
        register(dir, cls);
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