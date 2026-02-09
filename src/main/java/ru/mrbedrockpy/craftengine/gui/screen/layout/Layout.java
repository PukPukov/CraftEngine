package ru.mrbedrockpy.craftengine.gui.screen.layout;

import ru.mrbedrockpy.craftengine.CraftEngineClient;
import ru.mrbedrockpy.craftengine.event.client.CharTypeEvent;
import ru.mrbedrockpy.craftengine.gui.screen.widget.AbstractWidget;
import ru.mrbedrockpy.craftengine.render.gui.DrawContext;

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
                CraftEngineClient.INSTANCE.getCurrentScreen().setFocused(w);
                w.onMouseClick(x, y, button);
            }
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        widgets.values().stream().filter(AbstractWidget::isVisible).forEach(w -> w.render(context, mouseX, mouseY, delta));
    }

    public void moveWidgetsToLayout() {
        for (AbstractWidget w : widgets.values()) {
            w.setX(w.getX() + offsetX);
            w.setY(w.getY() + offsetY);
        }
    }

    public void onKeyPressed(int keyCode, int scanCode, int inputAction, int mods) {
        widgets.values().stream().filter(AbstractWidget::isVisible).forEach(w -> w.onKeyPressed(keyCode, scanCode, inputAction, mods));
    }

    public void onMouseScroll(double scrollX, double scrollY) {
        widgets.values().stream().filter(AbstractWidget::isVisible).forEach(w -> w.onMouseScroll(scrollX, scrollY));
    }

    public void charTyped(CharTypeEvent event) {
        widgets.values().stream().filter(AbstractWidget::isVisible).forEach(w -> w.charTyped(event.getCh(), event.getMods()));
    }

    public void tick() {
        widgets.values().stream().filter(AbstractWidget::isVisible).forEach(AbstractWidget::tick);
    }
}
