package ru.mrbedrockpy.craftengine.core.cfg;


import java.util.Map;

public class CraftEngineConfiguration {
    
    private static final Map<String, Object> DEFAULTS = Map.of(
            "render.distance", 8,
            "fov.dinamic_multiplier", 1.0f,
            "fov", 70.0f,
            "gui.scale", 6
    );
    
    public static final JsonConfig MAIN_CONFIG = JsonConfig.load("config/craftengine.json", DEFAULTS);
    
    public static void register() {}
    
}