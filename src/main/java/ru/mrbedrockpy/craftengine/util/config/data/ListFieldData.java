package ru.mrbedrockpy.craftengine.util.config.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Field;
import java.util.List;

@Getter
@AllArgsConstructor
public class ListFieldData<T> {

    private final String name;
    private final Class<T> type;
    private final Field field;

    public List<T> getValue() {
        try {
            return (List<T>) field.get(null);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setValue(List<T> value) {
        try {
            field.set(null, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
