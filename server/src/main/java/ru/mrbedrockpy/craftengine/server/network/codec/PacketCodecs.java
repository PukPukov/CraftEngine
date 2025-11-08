package ru.mrbedrockpy.craftengine.server.network.codec;

import io.netty.buffer.ByteBuf;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.core.data.WindowSettings;
import ru.mrbedrockpy.craftengine.core.registry.Registries;
import ru.mrbedrockpy.craftengine.core.world.block.Block;
import ru.mrbedrockpy.craftengine.server.network.packet.util.ByteBufUtil;
import ru.mrbedrockpy.craftengine.server.network.packet.util.VarInt;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// Кодеки которые будут повторятся из пакета в пакет и переиспользоватся
public class PacketCodecs{
    public static final Set<BufCodec<?>> ALL = new HashSet<>();
    private static final Map<Class<?>, BufCodec<?>> BY_CLASS = new java.util.concurrent.ConcurrentHashMap<>();

    public static final BufCodec<Vector3i> POS_CODEC = regT(
            BufCodec.of(Vector3i.class,
                    (buf, pos) -> { buf.writeInt(pos.x); buf.writeInt(pos.y); buf.writeInt(pos.z); },
                    buf -> new Vector3i(buf.readInt(), buf.readInt(), buf.readInt())
            )
    );

    public static final BufCodec<Block> BLOCK_CODEC = regT(
            BufCodec.of(Block.class,
                    (buf, block) -> VarInt.write(buf, Registries.BLOCKS.getId(block)),
                    buf -> Registries.BLOCKS.get(VarInt.read(buf))
            )
    );

    public static final BufCodec<Integer> INT   = regT(BufCodec.of(Integer.class, ByteBuf::writeInt, ByteBuf::readInt));
    public static final BufCodec<Long>    LONG  = regT(BufCodec.of(Long.class,    ByteBuf::writeLong, ByteBuf::readLong));
    public static final BufCodec<Boolean> BOOL  = regT(BufCodec.of(Boolean.class, ByteBuf::writeBoolean, ByteBuf::readBoolean));
    public static final BufCodec<Float>   FLOAT = regT(BufCodec.of(Float.class,   ByteBuf::writeFloat, ByteBuf::readFloat));
    public static final BufCodec<Double>  DBL   = regT(BufCodec.of(Double.class,  ByteBuf::writeDouble, ByteBuf::readDouble));
    public static final BufCodec<String>  STR   = regT(BufCodec.of(String.class,
            (buf, s) -> { byte[] a = s.getBytes(StandardCharsets.UTF_8); VarInt.write(buf, a.length); buf.writeBytes(a); },
            buf -> { int n = VarInt.read(buf); byte[] a = new byte[n]; buf.readBytes(a); return new String(a, java.nio.charset.StandardCharsets.UTF_8); }
    ));
    public static final BufCodec<java.util.UUID> UUID = regT(BufCodec.of(java.util.UUID.class,
            (buf, u) -> { buf.writeLong(u.getMostSignificantBits()); buf.writeLong(u.getLeastSignificantBits()); },
            buf -> new java.util.UUID(buf.readLong(), buf.readLong())
    ));

    public static <T> BufCodec<T> codecFor(Class<T> type) {
        @SuppressWarnings("unchecked")
        BufCodec<T> c = (BufCodec<T>) BY_CLASS.get(type);
        return c;
    }

    static BufCodec<?> require(Class<?> t) {
        BufCodec<?> c = BY_CLASS.get(t);
        if (c == null) throw new IllegalStateException("No BufCodec for " + t.getName());
        return c;
    }

    private static <T extends BufCodec<?>> T reg(T codec) {
        ALL.add(codec);
        var t = codec.type();
        if (t != null) BY_CLASS.putIfAbsent(t, codec);
        return codec;
    }
    private static <T> BufCodec<T> regT(BufCodec<T> c) { reg(c); return c; }
}
