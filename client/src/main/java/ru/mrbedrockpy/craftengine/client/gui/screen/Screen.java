package ru.mrbedrockpy.craftengine.client.gui.screen;

import ru.mrbedrockpy.craftengine.client.event.evt.KeyPressEvent;
import ru.mrbedrockpy.craftengine.client.event.evt.MouseClickEvent;
import ru.mrbedrockpy.craftengine.client.event.evt.MouseScrollEvent;
import ru.mrbedrockpy.craftengine.client.gui.screen.layout.Layout;
import ru.mrbedrockpy.renderer.gui.DrawContext;
import ru.mrbedrockpy.craftengine.client.gui.screen.widget.AbstractWidget;
import ru.mrbedrockpy.renderer.window.Window;

import java.util.*;

public class Screen {
    private final List<Layout> layouts = new ArrayList<>();

    public void init() {
    }

    public void close() {
        layouts.clear();
    }

    public void addLayout(Layout layout) {
        layouts.add(layout);
    }

    public <T extends AbstractWidget> T findWidget(String id, Class<T> type) {
        for (Layout layout : layouts) {
            AbstractWidget w = layout.findWidget(id);
            if (w != null && type.isInstance(w)) {
                return type.cast(w);
            }
        }
        return null;
    }

    public void onMouseClick(MouseClickEvent event) {
        int sx = scale((int) event.getX());
        int sy = scale((int) event.getY());
        for (Layout layout : List.copyOf(layouts)) {
            layout.onMouseClick(sx, sy, event.getButton());
        }
    }

    public void onKeyPressed(KeyPressEvent event){
        for (Layout layout : List.copyOf(layouts)){
            layout.onKeyPressed(event.getKeyCode());
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        for (Layout layout : layouts) {
            layout.render(context, mouseX, mouseY, delta);
        }
    }

    private int scale(int value) {
        return (int) (value / (float) Window.getWidth() * Window.scaledWidth());
    }

    public void onMouseScrolled(MouseScrollEvent event) {
        for (Layout layout : List.copyOf(layouts)){
            layout.onMouseScroll(event.getScrollX(), event.getScrollY());
        }
    }
}