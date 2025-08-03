package ru.mrbedrockpy.renderer.window;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WindowSettings {
    
    public static final WindowSettings DEFAULT = new WindowSettings(1280, 720, "CraftEngine", false, false);
    
    private int width;
    private int height;
    private String title;
    private boolean vsync;
    private boolean fullscreen;
    
}