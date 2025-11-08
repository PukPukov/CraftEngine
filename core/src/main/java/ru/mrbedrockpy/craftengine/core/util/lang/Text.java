package ru.mrbedrockpy.craftengine.core.util.lang;

public class Text {

    public static String translatable(String key) {
        return format(key);
    }

    public static String format(String text) {
        return TextColor.format(text);
    }

}
