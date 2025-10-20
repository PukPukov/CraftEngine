package ru.mrbedrockpy.craftengine.client.gui.screen.layout;

import ru.mrbedrockpy.craftengine.client.gui.screen.widget.AbstractWidget;
import ru.mrbedrockpy.renderer.gui.DrawContext;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Layout {
    protected final Map<String, AbstractWidget> widgets = new LinkedHashMap<>();

    protected int offsetX = 0;
    protected int offsetY = 0;

    public void setOffset(int x, int y) {
        this.offsetX = x;
        this.offsetY = y;
        moveWidgetsToLayout();
    }

    public void addWidget(String id, AbstractWidget widget) {
        widgets.put(id, widget);
        widget.setX(widget.getX() + offsetX);
        widget.setY(widget.getY() + offsetY);
    }

    public AbstractWidget findWidget(String id) {
        return widgets.get(id);
    }

    public void onMouseClick(int x, int y, int button) {
        for (AbstractWidget w : List.copyOf(widgets.values())) {
            if (w.isVisible() && w.isMouseOver(x, y)) {
                w.onMouseClick(x, y, button);
            }
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        for (AbstractWidget w : widgets.values()) {
            if (w.isVisible()) {
                w.render(context, mouseX, mouseY, delta);
            }
        }
    }

    public void moveWidgetsToLayout() {
        for (AbstractWidget w : widgets.values()) {
            w.setX(w.getX() + offsetX);
            w.setY(w.getY() + offsetY);
        }
    }

    public void onKeyPressed(int keyCode) {
        for(AbstractWidget w : widgets.values()){
            if(w.isVisible()){
                w.onKeyPressed(keyCode);
            }
        }
    }

    public void onMouseScroll(double scrollX, double scrollY) {
        for(AbstractWidget w : widgets.values()){
            if(w.isVisible()){
                w.onMouseScroll(scrollX, scrollY);
            }
        }
    }
}
