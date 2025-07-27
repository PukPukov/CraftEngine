package ru.mrbedrockpy.craftengine.gui.screen;

import ru.mrbedrockpy.craftengine.gui.screen.widget.ButtonWidget;
import ru.mrbedrockpy.craftengine.window.Window;

import java.awt.*;


public class MainMenuScreen extends Screen {
    @Override
    public void init() {
        addWidget(new ButtonWidget("Play", Window.getWidth() / 2 - 50, Window.getHeight() / 2 - 50, 100, 100, 0));
    }
}
