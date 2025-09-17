package ru.mrbedrockpy.craftengine.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.mrbedrockpy.renderer.RenderInit;
import ru.mrbedrockpy.renderer.api.IConfig;
import ru.mrbedrockpy.renderer.window.WindowSettings;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ConfigVars implements IConfig {


    private final Map<String, Object> map = new ConcurrentHashMap<>();

    public static final ConfigVars INSTANCE = new ConfigVars();

    private ConfigVars() {
        map.put("gui.scale", CraftEngineConfiguration.MAIN_CONFIG.getInt("gui.scale", 5));
        map.put("render.distance", CraftEngineConfiguration.MAIN_CONFIG.getInt("render.distance", 8));
        map.put("fov.dynamic_multiplier", CraftEngineConfiguration.MAIN_CONFIG.getFloat("fov.dynamic_multiplier", 1.0f));
        map.put("window.setting", CraftEngineConfiguration.MAIN_CONFIG.getObject("window.settings", WindowSettings.class, WindowSettings.DEFAULT));
        reloadFromFile();
        pushToRuntime();
    }

    @Override
    public int getInt(String key) {
        Object v = map.get(key);
        if (v instanceof Number n) return n.intValue();
        else return 0;
    }

    @Override
    public float getFloat(String key) {
        Object v = map.get(key);
        if (v instanceof Number n) return n.floatValue();
        else return 0.0f;
    }

    @Override
    @SuppressWarnings("unchecked")
    @NotNull
    public <T> T getObject(String key, Class<T> type) {
        Object o = map.get(key);
        return type.isInstance(o) ? (T) o : null;
    }

    @Override
    public Map<String, Object> asMap() {
        return Map.copyOf(map);
    }

    private void reloadFromFile() {
    }

    public void pushToRuntime() {
        map.put("gui.scale", CraftEngineConfiguration.MAIN_CONFIG.getInt("gui.scale", 6));
        map.put("render.distance", CraftEngineConfiguration.MAIN_CONFIG.getInt("render.distance", 8));
        map.put("fov.dynamic_multiplier", CraftEngineConfiguration.MAIN_CONFIG.getFloat("fov.dynamic_multiplier", 1.0f));
        map.put("fov", CraftEngineConfiguration.MAIN_CONFIG.getFloat("fov", 70.0f));
        map.put("window.settings", CraftEngineConfiguration.MAIN_CONFIG.getObject("window.settings", WindowSettings.class, WindowSettings.DEFAULT));
        RenderInit.CONFIG = this;
    }
}