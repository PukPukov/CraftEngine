package ru.mrbedrockpy.craftengine.client.gui.screen;

import ru.mrbedrockpy.craftengine.client.event.client.input.CharTypeEvent;
import ru.mrbedrockpy.craftengine.client.event.client.input.KeyPressEvent;
import ru.mrbedrockpy.craftengine.client.event.client.input.MouseClickEvent;
import ru.mrbedrockpy.craftengine.client.event.client.input.MouseScrollEvent;
import ru.mrbedrockpy.craftengine.client.gui.screen.layout.Layout;
import ru.mrbedrockpy.craftengine.core.util.id.RL;
import ru.mrbedrockpy.renderer.gui.DrawContext;
import ru.mrbedrockpy.craftengine.client.gui.screen.widget.AbstractWidget;
import ru.mrbedrockpy.renderer.window.Window;

import java.util.*;

public class Screen{
    protected final int width = Window.scaledWidth(), height = Window.scaledHeight();
    protected AbstractWidget focused;
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
            if (type.isInstance(w)) {
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

    // Какая то магия
    private int scale(int value) {
        return (int) (value / (float) Window.getWidth() * Window.scaledWidth());
    }

    public void onKeyPressed(KeyPressEvent event) {
        for (Layout layout : List.copyOf(layouts)) {
            layout.onKeyPressed(event.getKeyCode(), event.getScanCode(), event.getInputAction(), event.getMods());
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        for (Layout layout : layouts) {
            layout.render(context, mouseX, mouseY, delta);
        }
    }

    protected void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTexture(0, 0, width, height, RL.of("gui/background.png"));
    }

    public void onMouseScrolled(MouseScrollEvent event) {
        for (Layout layout : List.copyOf(layouts)) {
            layout.onMouseScroll(event.getScrollX(), event.getScrollY());
        }
    }

    public void setFocused(AbstractWidget widget) {
        if (focused != null) focused.setFocused(false);
        focused = widget;
    }

    public void charType(CharTypeEvent event) {
        for (Layout layout : layouts) {
            layout.charTyped(event);
        }
    }

    public void tick() {
       layouts.forEach(Layout::tick);
    }
}