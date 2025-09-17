package ru.mrbedrockpy.renderer.api;

public interface IConfig {

    int getInt(String key);
    float getFloat(String key);
    <T> T getObject(String key, Class<T> type);

    java.util.Map<String, Object> asMap();

}
