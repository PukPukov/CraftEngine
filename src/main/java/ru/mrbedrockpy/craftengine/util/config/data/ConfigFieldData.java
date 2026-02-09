package ru.mrbedrockpy.craftengine.util.config.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Field;

@Getter
@AllArgsConstructor
public class ConfigFieldData<T> {

    private final String name;
    private final Class<T> type;
    private final Field field;

    public T getValue() {
        try {
            return (T) field.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setValue(Object value) {
        try {
            field.set(null, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
