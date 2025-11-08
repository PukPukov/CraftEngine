package ru.mrbedrockpy.craftengine.core.util.config;

import ru.mrbedrockpy.craftengine.core.data.WindowSettings;
import ru.mrbedrockpy.craftengine.core.util.config.annotation.Config;
import ru.mrbedrockpy.craftengine.core.util.config.annotation.ConfigField;

@Config(name = "config.yml")
public class CraftEngineConfig {

    @ConfigField(name = "render-distance")
    public static int RENDER_DISTANCE = 8;

    @ConfigField(name = "fov")
    public static float FOV = 110.0f;

    @ConfigField(name = "fov-dynamic-multiplier")
    public static float FOV_DYNAMIC_MULTIPLIER = 1.0f;

    @ConfigField(name = "gui-scale")
    public static int GUI_SCALE = 5;

    @Config(name = "network")
    public static class Network {
        @ConfigField(name = "name")
        public static String NAME = "Dev";
        @ConfigField(name = "last_ip")
        public static String LAST_IP = "";
    }

    @ConfigField(name = "window")
    public static WindowSettings WINDOW = WindowSettings.DEFAULT;

}