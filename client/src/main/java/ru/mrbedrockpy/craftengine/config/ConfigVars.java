package ru.mrbedrockpy.craftengine.config;

import ru.mrbedrockpy.renderer.RenderVars;
import ru.mrbedrockpy.renderer.window.WindowSettings;

public class ConfigVars {
    
    public static int GUI_SCALE;
    public static int RENDER_DISTANCE;
    public static float FOV_CHANGE_MULTIPLIER;
    public static WindowSettings WINDOW_SETTINGS;
    public static void update() {
        GUI_SCALE = CraftEngineConfiguration.MAIN_CONFIG.getInt("gui.scale", 5);
        WINDOW_SETTINGS = CraftEngineConfiguration.MAIN_CONFIG.getObject("window.settings", WindowSettings.class, WindowSettings.DEFAULT);
        RENDER_DISTANCE = CraftEngineConfiguration.MAIN_CONFIG.getInt("render.distance", 8);
        FOV_CHANGE_MULTIPLIER = CraftEngineConfiguration.MAIN_CONFIG.getFloat("fov.dinamic_multiplier", 1.0f);
        RenderVars.RENDER_DISTANCE = RENDER_DISTANCE;
        RenderVars.GUI_SCALE = GUI_SCALE;
    }
    
}