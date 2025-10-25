package ru.mrbedrockpy.craftengine.core.util.config;

import ru.mrbedrockpy.craftengine.core.util.config.annotation.Config;
import ru.mrbedrockpy.craftengine.core.util.config.annotation.ConfigField;

@Config(name = "config.yml")
public class CraftEngineConfig {
    
    @ConfigField(name = "render-distance")
    public static int RENDER_DISTANCE = 8;
    
    @ConfigField(name = "fov")
    public static float FOV = 70.0f;
    
    @ConfigField(name = "fov-dynamic-multiplier")
    public static float FOV_DYNAMIC_MULTIPLIER = 1.0f;
    
    @ConfigField(name = "gui-scale")
    public static int guiScale = 6;
    
}