package ru.mrbedrockpy.craftengine.server.network.codec;

import io.netty.buffer.ByteBuf;

import java.util.function.BiConsumer;
import java.util.function.Function;

public interface BufCodec<T>{
    Class<T> type();
    void encode(T value, ByteBuf out);
    T decode(ByteBuf in);

    static <T> BufCodec<T> of(Class<T> type,BiConsumer<ByteBuf, T> e, Function<ByteBuf, T> d) {
        return new BufCodec<>() {
            @Override
            public Class<T> type() {
                return type;
            }

            public void encode(T v, ByteBuf out) { e.accept(out, v); }
            public T decode(ByteBuf in)          { return d.apply(in); }
        };
    }
}
