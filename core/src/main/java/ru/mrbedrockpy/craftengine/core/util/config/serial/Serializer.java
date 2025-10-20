package ru.mrbedrockpy.craftengine.core.util.config.serial;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

@Getter
@AllArgsConstructor
public class Serializer<T> {

    private final Class<T> dataType;
    private final Function<T, String> serializer;
    private final Function<String, T> deserializer;

    public String serialize(T object) {
        return serializer.apply(object);
    }

    public T deserialize(String data) {
        return deserializer.apply(data);
    }
}
