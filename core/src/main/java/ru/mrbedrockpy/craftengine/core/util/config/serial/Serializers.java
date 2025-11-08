package ru.mrbedrockpy.craftengine.core.util.config.serial;

import ru.mrbedrockpy.craftengine.core.data.WindowSettings;

import java.util.ArrayList;
import java.util.List;

public class Serializers {

    public static List<Serializer<?>> getSerializers() {
        return new ArrayList<>(List.of(
                getPrimitiveByteSerializer(),
                getPrimitiveShortSerializer(),
                getPrimitiveIntSerializer(),
                getPrimitiveLongSerializer(),
                getPrimitiveFloatSerializer(),
                getPrimitiveDoubleSerializer(),
                getPrimitiveBoolSerializer(),
                getPrimitiveCharSerializer(),
                getByteSerializer(),
                getShortSerializer(),
                getIntegerSerializer(),
                getLongSerializer(),
                getFloatSerializer(),
                getDoubleSerializer(),
                getBooleanSerializer(),
                getCharacterSerializer(),
                getStringSerializer(),
                getWindowsSettingsSerilizer()
        ));
    }

    private static Serializer<WindowSettings> getWindowsSettingsSerilizer() {
        return new Serializer<>(WindowSettings.class, Object::toString, WindowSettings::fromString);
    }

    public static Serializer<String> getStringSerializer() {
        return new Serializer<>(String.class, s -> s, s -> s);
    }

    public static Serializer<Byte> getPrimitiveByteSerializer() {
        return new Serializer<>(byte.class, String::valueOf, Byte::parseByte);
    }

    public static Serializer<Short> getPrimitiveShortSerializer() {
        return new Serializer<>(short.class, String::valueOf, Short::parseShort);
    }

    public static Serializer<Integer> getPrimitiveIntSerializer() {
        return new Serializer<>(int.class, String::valueOf, Integer::parseInt);
    }

    public static Serializer<Long> getPrimitiveLongSerializer() {
        return new Serializer<>(long.class, String::valueOf, Long::parseLong);
    }

    public static Serializer<Float> getPrimitiveFloatSerializer() {
        return new Serializer<>(float.class, String::valueOf, Float::parseFloat);
    }

    public static Serializer<Double> getPrimitiveDoubleSerializer() {
        return new Serializer<>(double.class, String::valueOf, Double::parseDouble);
    }

    public static Serializer<Boolean> getPrimitiveBoolSerializer() {
        return new Serializer<>(boolean.class, String::valueOf, Boolean::parseBoolean);
    }

    public static Serializer<Character> getPrimitiveCharSerializer() {
        return new Serializer<>(char.class, String::valueOf, s -> s.charAt(0));
    }

    public static Serializer<Byte> getByteSerializer() {
        return new Serializer<>(Byte.class, String::valueOf, Byte::parseByte);
    }

    public static Serializer<Short> getShortSerializer() {
        return new Serializer<>(Short.class, String::valueOf, Short::parseShort);
    }

    public static Serializer<Integer> getIntegerSerializer() {
        return new Serializer<>(Integer.class, String::valueOf, Integer::parseInt);
    }

    public static Serializer<Long> getLongSerializer() {
        return new Serializer<>(Long.class, String::valueOf, Long::parseLong);
    }

    public static Serializer<Float> getFloatSerializer() {
        return new Serializer<>(Float.class, String::valueOf, Float::parseFloat);
    }

    public static Serializer<Double> getDoubleSerializer() {
        return new Serializer<>(Double.class, String::valueOf, Double::parseDouble);
    }

    public static Serializer<Boolean> getBooleanSerializer() {
        return new Serializer<>(Boolean.class, String::valueOf, Boolean::parseBoolean);
    }

    public static Serializer<Character> getCharacterSerializer() {
        return new Serializer<>(Character.class, String::valueOf, s -> s.charAt(0));
    }
}
