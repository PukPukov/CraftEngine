package ru.mrbedrockpy.craftengine.core.util.lang;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TextColor {

    RESET("&0", "\u001B[0m"),
    BLACK("&1", "\u001B[30m"),
    RED("&2", "\u001B[31m"),
    GREEN("&3", "\u001B[32m"),
    YELLOW("&4", "\u001B[33m"),
    BLUE("&5", "\u001B[34m"),
    PURPLE("&6", "\u001B[35m"),
    CYAN("&7", "\u001B[36m"),
    WHITE("&8", "\u001B[37m");

    private final String colorCode;
    private final String ansiCode;

    public static String format(String text) {
        for (TextColor color : values()) {
            text = text.replace(color.getColorCode(), color.getAnsiCode());
        }
        return text + RESET.ansiCode;
    }

    @Override
    public String toString() {
        return ansiCode;
    }
}
