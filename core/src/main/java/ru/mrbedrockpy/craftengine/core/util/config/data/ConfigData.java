package ru.mrbedrockpy.craftengine.core.util.config.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConfigData {

    private final String name;
    private final ConfigFieldData<?>[] fields;
    private final ListFieldData<?>[] listFields;
    private final ConfigData[] categories;

}
