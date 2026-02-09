package ru.mrbedrockpy.craftengine.gui.screen.widget;

import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import ru.mrbedrockpy.craftengine.CraftEngineClient;
import ru.mrbedrockpy.craftengine.util.lang.Component;
import ru.mrbedrockpy.craftengine.renderer.font.ComponentRenderer;
import ru.mrbedrockpy.craftengine.renderer.gui.DrawContext;

import java.awt.*;

public class TextField extends AbstractWidget {
    @Getter
    private String text = "";
    private int cursorOffset = 0;
    private static final int CURSOR_BLINK_RATE = 20;

    public TextField(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ComponentRenderer cr = context.getFontRenderer();
        context.drawText(text, x + 2, y + 4);

        long tick = CraftEngineClient.INSTANCE.getTickSystem().getCurrentTick();
        if (tick % CURSOR_BLINK_RATE < CURSOR_BLINK_RATE / 2) {
            String beforeCursor = text.substring(0, Math.min(cursorOffset, text.length()));
            float cursorX = x + 2 + cr.getTextSize(Component.literal(beforeCursor)).x;

            context.drawRect((int) cursorX, y + 3, 1, 10, new Color(0xFFFFFFFF, true));
        }
    }

    @Override
    public void charTyped(int c, int mods) {
        super.charTyped(c, mods);
        if (Character.isISOControl(c)) return;

        char newChar = (char) c;
        if (cursorOffset < 0) cursorOffset = 0;
        if (cursorOffset > text.length()) cursorOffset = text.length();

        text = text.substring(0, cursorOffset) + newChar + text.substring(cursorOffset);
        cursorOffset++;
    }

    @Override
    public void onKeyPressed(int keyCode, int scanCode, int inputAction, int mods) {
        super.onKeyPressed(keyCode, scanCode, inputAction, mods);

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (cursorOffset > 0 && !text.isEmpty()) {
                text = text.substring(0, cursorOffset - 1) + text.substring(cursorOffset);
                cursorOffset--;
            }
        }
        else if (keyCode == GLFW.GLFW_KEY_DELETE) {
            if (cursorOffset < text.length()) {
                text = text.substring(0, cursorOffset) + text.substring(cursorOffset + 1);
            }
        }
        else if (keyCode == GLFW.GLFW_KEY_LEFT) {
            if (cursorOffset > 0) cursorOffset--;
        }
        else if (keyCode == GLFW.GLFW_KEY_RIGHT) {
            if (cursorOffset < text.length()) cursorOffset++;
        }
        else if (keyCode == GLFW.GLFW_KEY_HOME) {
            cursorOffset = 0;
        }
        else if (keyCode == GLFW.GLFW_KEY_END) {
            cursorOffset = text.length();
        }
    }

    public void clear() {
        text = "";
        cursorOffset = 0;
    }

    public void setText(String text) {
        this.text = text;
        cursorOffset = text.length();
    }

}