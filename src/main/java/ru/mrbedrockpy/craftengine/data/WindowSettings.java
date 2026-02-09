package ru.mrbedrockpy.craftengine.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.mrbedrockpy.craftengine.util.Logger;

@Getter
@AllArgsConstructor
public class WindowSettings {

    public static final WindowSettings DEFAULT = new WindowSettings(1920, 1080, "CraftEngine", true, true);
    private static final Logger LOGGER = Logger.getLogger(WindowSettings.class);

    private final int width;
    private final int height;
    private final String title;
    private final boolean vsync;
    private final boolean fullscreen;

    @Override
    public String toString() {
        return width + "," + height + "," + title  + "," + vsync + "," + fullscreen;
    }

    public static WindowSettings fromString(String s) {
        String[] parts = s.split(",");
        try {
            int width = Integer.parseInt(parts[0]);
            int height = Integer.parseInt(parts[1]);
            String title = parts[2];
            boolean vsync = Boolean.parseBoolean(parts[3]);
            boolean fullscreen = Boolean.parseBoolean(parts[4]);
            return new WindowSettings(width, height, title, vsync, fullscreen);
        } catch (NumberFormatException e) {
            LOGGER.warn("Invalid symbols in window settings! Use default settings!", e);
            return DEFAULT;
        } catch (IndexOutOfBoundsException e) {
            LOGGER.warn("Invalid count of arguments! Use default settings!", e);
            return DEFAULT;
        }
    }
}