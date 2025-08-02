package ru.mrbedrockpy.craftengine.config;

import ru.mrbedrockpy.renderer.window.Window;
import ru.mrbedrockpy.renderer.window.WindowSettings;

import java.util.Map;

public class CraftEngineConfiguration {
    private static final Map<String, Object> DEFAULTS = Map.of(
            "render.distance", 8,
            "window.settings", WindowSettings.DEFAULT,
            "fov.dinamic_multiplier", 1.0f
    );
    public static final JsonConfig MAIN_CONFIG = JsonConfig.load("config/craftengine.json", DEFAULTS);
    public static void register() {}
}
