package ru.mrbedrockpy.craftengine.server.network.codec;

import io.netty.buffer.ByteBuf;
import ru.mrbedrockpy.craftengine.server.network.packet.Packet;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface PacketCodec<P> {
    void encode(P pkt, ByteBuf out);
    P decode(ByteBuf in);

    static <P extends Packet> PacketCodec<P> of(
            BiConsumer<P, ByteBuf> encoder,
            Function<ByteBuf, P> decoder
    ) {
        return new PacketCodec<>() {
            @Override
            public void encode(P pkt, ByteBuf out) {
                encoder.accept(pkt, out);
            }

            @Override
            public P decode(ByteBuf in) {
                return decoder.apply(in);
            }
        };
    }

    static <P extends Packet> PacketCodec<P> forRecord(
            Class<P> type,
            Function<Class<?>, BufCodec<?>> codecLookup
    ) {
        if (!type.isRecord())
            throw new IllegalArgumentException(type + " is not a record");

        RecordComponent[] comps = type.getRecordComponents();
        Class<?>[] ctorTypes = Arrays.stream(comps).map(RecordComponent::getType).toArray(Class[]::new);

        final Constructor<P> ctor;
        try {
            ctor = type.getDeclaredConstructor(ctorTypes);
            ctor.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot get canonical constructor of " + type, e);
        }

        final var accessors = Arrays.stream(comps).map(RecordComponent::getAccessor).toArray(java.lang.reflect.Method[]::new);
        final BufCodec<?>[] fieldCodecs = Arrays.stream(comps)
                .map(RecordComponent::getType)
                .map(t -> {
                    BufCodec<?> c = codecLookup.apply(t);
                    if (c == null)
                        throw new IllegalStateException("No BufCodec registered for field type: " + t.getName()
                                + " (in " + type.getName() + ")");
                    return c;
                })
                .toArray(BufCodec<?>[]::new);

        return PacketCodec.of(
                // encoder: пробегаем поля рекорда и кодируем по их BufCodec
                (pkt, out) -> {
                    try {
                        for (int i = 0; i < accessors.length; i++) {
                            Object fv = accessors[i].invoke(pkt);
                            @SuppressWarnings("unchecked")
                            BufCodec<Object> fc = (BufCodec<Object>) fieldCodecs[i];
                            fc.encode(fv, out);
                        }
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException("Encoding failed for " + type.getName(), e);
                    }
                },
                // decoder: читаем поля по порядку и вызываем канонический конструктор
                in -> {
                    Object[] args = new Object[fieldCodecs.length];
                    for (int i = 0; i < fieldCodecs.length; i++) {
                        @SuppressWarnings("unchecked")
                        BufCodec<Object> fc = (BufCodec<Object>) fieldCodecs[i];
                        args[i] = fc.decode(in);
                    }
                    try {
                        return ctor.newInstance(args);
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException("Decoding failed for " + type.getName(), e);
                    }
                }
        );
    }

    /**
     * Удобный перегруз — ищем BufCodec через ваш реестр.
     * Замените внутри на свой способ поиска (например, BufCodecRegistry.require(type)).
     */
    static <P extends Packet> PacketCodec<P> forRecord(Class<P> type) {
        return forRecord(type, PacketCodecs::require); // пример: ваш статический реестр;;
    }
}