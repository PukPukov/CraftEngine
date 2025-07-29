package ru.mrbedrockpy.renderer.window;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WindowSettings {

    private int width;
    private int height;
    private String title;
    private boolean vsync;
    private boolean fullscreen;

}
