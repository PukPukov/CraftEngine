package ru.mrbedrockpy.craftengine.client.gui.screen.layout;

import ru.mrbedrockpy.craftengine.client.gui.screen.widget.AbstractWidget;

public class VerticalCenterStackLayout extends Layout {
    @Override
    public void moveWidgetsToLayout() {
        int tempHeight = 0;
        for (AbstractWidget w : widgets.values()) {
            w.setX(offsetX - w.getWidth() / 2);
            w.setY(offsetY - w.getHeight() / 2 + tempHeight);
            tempHeight += w.getHeight() + 2;
        }
    }
}