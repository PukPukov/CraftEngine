package ru.mrbedrockpy.concept.api.util;

public interface IStyle {

    IFont getFont();

    void setFont(IFont font);

    IColor getColor();

    void setColor(IColor color);

    boolean isBold();

    void setBold(boolean bold);

    boolean isItalic();

    void setItalic(boolean italic);

    int getFontSize();

    void setFontSize(int fontSize);

}
