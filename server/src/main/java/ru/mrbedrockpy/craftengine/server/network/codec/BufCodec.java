package ru.mrbedrockpy.craftengine.server.network.codec;

import io.netty.buffer.ByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface BufCodec<T> {
    void encode(T value, ByteBuf out);
    T decode(ByteBuf in);

    static <T> BufCodec<T> of(BiConsumer<T, ByteBuf> e, Function<ByteBuf, T> d) {
        return new BufCodec<>() {
            public void encode(T v, ByteBuf out) { e.accept(v, out); }
            public T decode(ByteBuf in)          { return d.apply(in); }
        };
    }
}
