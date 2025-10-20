package ru.mrbedrockpy.craftengine.core.util.config;

import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import ru.mrbedrockpy.craftengine.core.util.FileUtil;
import ru.mrbedrockpy.craftengine.core.util.Logger;
import ru.mrbedrockpy.craftengine.core.util.config.annotation.Config;
import ru.mrbedrockpy.craftengine.core.util.config.annotation.ConfigField;
import ru.mrbedrockpy.craftengine.core.util.config.annotation.ListField;
import ru.mrbedrockpy.craftengine.core.util.config.data.ConfigData;
import ru.mrbedrockpy.craftengine.core.util.config.data.ConfigFieldData;
import ru.mrbedrockpy.craftengine.core.util.config.data.ListFieldData;
import ru.mrbedrockpy.craftengine.core.util.config.serial.Serializers;
import ru.mrbedrockpy.craftengine.core.util.config.serial.Serializer;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public final class ConfigManager {

    private final Yaml yaml = new Yaml(getDumperOptions());

    private final Logger logger = Logger.getLogger(ConfigManager.class);
    private final List<Serializer<?>> serializers = Serializers.getPrimitiveSerializers();

    private final File configFolder;

    private final ConfigData[] configs;

    private ConfigManager(File configFolder, Class<?>... configs) {
        this.configFolder = configFolder;
        this.configs = Arrays.stream(configs)
                .map(this::initConfig)
                .filter(Objects::nonNull)
                .toArray(ConfigData[]::new);
        this.loadConfigs();
    }

    private ConfigData initConfig(Class<?> clazz) {
        Config configAnnotation = clazz.getAnnotation(Config.class);
        if (configAnnotation == null) {
            logger.error(clazz.getName() + " is not annotated with @Config");
            return null;
        }
        List<ConfigFieldData<?>> fields = new ArrayList<>();
        List<ListFieldData<?>> listFields = new ArrayList<>();
        for (Field field: clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            ConfigField fieldAnnotation = field.getAnnotation(ConfigField.class);
            if (fieldAnnotation != null) {
                ConfigFieldData<?> configFieldData = initConfigField(fields, fieldAnnotation, field);
                if (configFieldData != null) fields.add(configFieldData);
            } else {
                ListField listAnnotation = field.getAnnotation(ListField.class);
                if (listAnnotation == null) continue;
                ListFieldData<?> listFieldData = initListField(listAnnotation, field);
                if (listFieldData != null) listFields.add(listFieldData);
            }
        }
        return new ConfigData(
                configAnnotation.name(),
                fields.toArray(new ConfigFieldData[0]),
                listFields.toArray(new ListFieldData[0]),
                Arrays.stream(clazz.getDeclaredClasses())
                        .map(this::initConfig)
                        .filter(Objects::nonNull)
                        .toArray(ConfigData[]::new)
        );
    }

    private ConfigFieldData<?> initConfigField(List<ConfigFieldData<?>> fields, ConfigField annotation, Field field) {
        String name = annotation.name();
        field.setAccessible(true);
        try {
            Object value = field.get(null);
            if (value == null) {
                logger.warn("Field cannot be null: " + name);
                return null;
            }
            return new ConfigFieldData<>(name, field.getType(), field);
        } catch (IllegalAccessException e) {
            logger.warn(name + "'s access error : " + e.getMessage());
            return null;
        }
    }
    
    private <T> ListFieldData<T> initListField(ListField annotation, Field field) {
        field.setAccessible(true);
        try {
            Object value = field.get(null);
            if (value == null) {
                logger.warn("Field cannot be null: " + annotation.name());
                return null;
            }
            return new ListFieldData<>(annotation.name(), (Class<T>) annotation.type(), field);
        } catch (IllegalAccessException e) {
            logger.warn(annotation.name() + "'s access error : " + e.getMessage());
            return null;
        }
    }

    public void loadConfigs() {
        for (ConfigData config : configs) {
            File configFile = new File(configFolder, config.getName());
            String content = FileUtil.getTextFile(configFile);
            Map<String, Object> data;
            if (content == null || content.isEmpty()) data = new HashMap<>();
            else {
                data = yaml.load(content);
                if (data == null) data = new HashMap<>();
            }
            if (data.isEmpty()) saveConfigs();
            else loadConfig(config, data);
        }
    }

    private void loadConfig(ConfigData configData, Map<String, Object> data) {
        for (ConfigFieldData<?> fieldData : configData.getFields()) {
            if (data.containsKey(fieldData.getName())) {
                try {
                    String value = String.valueOf(data.get(fieldData.getName()));
                    Class<?> type = fieldData.getType();
                    if (type.equals(List.class)) {
                        fieldData.setValue(serializeList((List<?>) fieldData.getValue(), fieldData.getType()));
                    }
                    else {
                        Serializer<?> serializer = getSerializer(type);
                        if (serializer == null) throw new RuntimeException("Serializer not found: " + fieldData.getType().getName());
                        fieldData.setValue(serializer.deserialize(value));
                    }
                } catch (Exception e) {
                    logger.error("Failed to set value for field " + fieldData.getName() + ": " + e.getMessage());
                }
            }
        }
        for (ConfigData category : configData.getCategories()) {
            Object categoryData = data.get(category.getName());
            if (categoryData instanceof Map) {
                loadConfig(category, (Map<String, Object>) categoryData);
            }
        }
    }

    public void saveConfigs() {
        for (ConfigData config : configs) {
            Map<String, Object> data = new LinkedHashMap<>();
            try {
                saveConfig(config, data);
                FileUtil.setTextFile(new File(configFolder, config.getName()), yaml.dump(data));
            } catch (Exception e) {
                logger.error("Failed to save config " + config.getName() + ": " + e.getMessage());
            }
        }
    }

    private void saveConfig(ConfigData configData, Map<String, Object> data) {
        for (ConfigFieldData<?> fieldData : configData.getFields()) {
            try {
                Serializer<?> serializer = getSerializer(fieldData.getValue().getClass());
                if (serializer == null) throw new RuntimeException("Serializer not found: " + fieldData.getType().getName());
                data.put(fieldData.getName(), serialize(serializer, fieldData.getValue()));
            } catch (Exception e) {
                logger.error("Failed to get value for field " + fieldData.getName() + ": " + e.getMessage());
            }
        }
        for (ConfigData category : configData.getCategories()) {
            Map<String, Object> categoryData = new LinkedHashMap<>();
            saveConfig(category, categoryData);
            data.put(category.getName(), categoryData);
        }
    }

    private <T> String serialize(Serializer<T> serializer, Object value) {
        return serializer.serialize((T) value);
    }

    @Nullable
    private <T> Serializer<T> getSerializer(Class<T> clazz) {
        for (Serializer<?> serializer : serializers) {
            if (serializer.getDataType().equals(clazz)) return (Serializer<T>) serializer;
        }
        return null;
    }

    private <T> List<String> serializeList(List<?> list, Class<?> clazz) {
        Serializer<T> serializer = getSerializer((Class<T>) clazz);
        if (serializer == null) throw new RuntimeException("Serializer not found: " + clazz.getName());
        List<String> result = new ArrayList<>();
        ((List<T>) list).forEach(item -> result.add(serialize(serializer, item)));
        return result;
    }

    private <T> List<T> deserializeList(List<String> list, Class<T> clazz) {
        Serializer<T> serializer = getSerializer(clazz);
        if (serializer == null) throw new RuntimeException("Serializer not found: " + clazz.getName());
        List<T> result = new ArrayList<>();
        list.forEach(item -> result.add(serializer.deserialize(item)));
        return result;
    }

    private DumperOptions getDumperOptions() {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        options.setPrettyFlow(true);
        options.setIndent(2);
        options.setSplitLines(false);
        return options;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private File parentFolder;
        private Class<?>[] configs;

        private Builder() {}

        public Builder setParentFolder(File parentFolder) {
            this.parentFolder = parentFolder;
            return this;
        }

        public Builder setConfigs(Class<?>... configs) {
            this.configs = configs;
            return this;
        }

        public ConfigManager build() {
            return new ConfigManager(parentFolder, configs);
        }
    }
}
