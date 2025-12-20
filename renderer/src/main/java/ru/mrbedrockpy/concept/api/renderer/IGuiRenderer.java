package ru.mrbedrockpy.concept.api.renderer;

import ru.mrbedrockpy.concept.api.util.IColor;
import ru.mrbedrockpy.concept.api.util.IFont;
import ru.mrbedrockpy.concept.api.util.IStyle;

public interface IGuiRenderer {

    void renderTexture(int x, int y, int width, int height, String texturePath);

    void renderTexture(int x, int y, int width, int height, int u, int v, String texturePath);

    void renderTexture(int x, int y, int screenWidth, int screenHeight, int u, int v, int sourceWidth, int sourceHeight, String texturePath);

    void renderRect(int x, int y, int width, int height, IColor color);

    void renderText(String text, int x, int y);

    void renderText(String text, int x, int y, IStyle style);

    void renderText(String text, int x, int y, int fontSize);

    void renderText(String text, int x, int y, IFont font);

    void renderText(String text, int x, int y, IFont font, int fontSize);

    void renderText(String text, int x, int y, IColor color);

    void renderText(String text, int x, int y, IColor color, int fontSize);

    void renderText(String text, int x, int y, IFont font, IColor color);

    void renderText(String text, int x, int y, IFont font, IColor color, int fontSize);

}
