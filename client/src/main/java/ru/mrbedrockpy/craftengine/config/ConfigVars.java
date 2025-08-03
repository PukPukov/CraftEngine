package ru.mrbedrockpy.craftengine.config;

import ru.mrbedrockpy.renderer.window.WindowSettings;

public class ConfigVars {
    
    public static int GUI_SCALE;
    public static WindowSettings WINDOW_SETTINGS;
    public static void update() {
        GUI_SCALE = CraftEngineConfiguration.MAIN_CONFIG.getInt("gui.scale", 6);
        WINDOW_SETTINGS = CraftEngineConfiguration.MAIN_CONFIG.getObject("window.settings", WindowSettings.class, WindowSettings.DEFAULT);
    }
    
}