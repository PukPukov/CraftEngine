package ru.mrbedrockpy.craftengine.gui.screen;

import ru.mrbedrockpy.craftengine.event.MouseClickEvent;
import ru.mrbedrockpy.craftengine.gui.screen.callback.RenderCallback;
import ru.mrbedrockpy.renderer.gui.DrawContext;
import ru.mrbedrockpy.craftengine.gui.screen.widget.AbstractWidget;
import ru.mrbedrockpy.renderer.window.Input;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class Screen {
    private final List<AbstractWidget> widgets = new ArrayList<>();
    private Runnable onInit;
    private RenderCallback onRender;

    public void init(){
        if(onInit != null) {
            onInit.run();
        }
    }

    public void close() {
        Input.closeGUI();
    }

    public void addWidget(AbstractWidget widget) {
        widgets.add(widget);
        widgets.sort(Comparator.comparingInt(AbstractWidget::zIndex));
        widgets.indexOf(widget);
    }
    
    public void removeWidget(int index) {
        widgets.remove(index);
    }

    public void onMouseClick(MouseClickEvent event) {
        if(Input.isGUIOpen()) {
            for (AbstractWidget widget : widgets) {
                if (widget.visible() && widget.isMouseOver((int) event.x(), (int) event.y())) {
                    widget.onMouseClick((int) event.x(), (int) event.y(), event.button());
                }
            }
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        for(AbstractWidget widget : widgets) {
            if (widget.visible()) {
                widget.render(context, mouseX, mouseY, delta);
            }
        }
        if(onRender != null){
            onRender.render(context, mouseX, mouseY, delta);
        }
    }

    public void setInitCallback(Runnable onInit) {
        this.onInit = onInit;
    }

    public void setRenderCallback(RenderCallback onRender){
        this.onRender = onRender;
    }
}