package ru.mrbedrockpy.craftengine.core.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class WindowSettings{

    public static final WindowSettings DEFAULT = new WindowSettings(1920, 1080, "CraftEngine", true, true);

    private int width;
    private int height;
    private String title;
    private boolean vsync;
    private boolean fullscreen;

    @Override
    public String toString() {
        return width + "," + height + "," + title  + "," + vsync + "," + fullscreen;
    }

    public static WindowSettings fromString(String s) {
        String[] parts = s.split(",");
        int width = Integer.parseInt(parts[0]);
        int height = Integer.parseInt(parts[1]);
        String title = parts[2];
        boolean vsync = Boolean.parseBoolean(parts[3]);
        boolean fullscreen = Boolean.parseBoolean(parts[4]);
        return new WindowSettings(width, height, title, vsync, fullscreen);
    }
}