package ru.mrbedrockpy.renderer.world.window;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WindowSettings {
    
    public static final WindowSettings DEFAULT = new WindowSettings(1920, 1080, "CraftEngine", true, true);
    
    private int width;
    private int height;
    private String title;
    private boolean vsync;
    private boolean fullscreen;
    
}