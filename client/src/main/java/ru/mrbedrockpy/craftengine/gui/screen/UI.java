package ru.mrbedrockpy.craftengine.gui.screen;

import ru.mrbedrockpy.craftengine.gui.screen.callback.RenderCallback;
import ru.mrbedrockpy.craftengine.gui.screen.widget.AbstractWidget;
import ru.mrbedrockpy.craftengine.gui.screen.widget.ButtonWidget;

import java.util.function.Consumer;

public class UI {
    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private final Screen screen = new Screen();
        private Runnable onInit;
        private RenderCallback onRender;

        public Builder add(AbstractWidget widget) {
            screen.addWidget(widget);
            return this;
        }

        public Builder button(String text, int x, int y, int width, int height, Consumer<ButtonWidget> onClick){
            screen.addWidget(new ButtonWidget(text, x, y, width, height, onClick));
            return this;
        }

        public Builder init(Runnable runnable) {
            this.onInit = runnable;
            return this;
        }

        public Builder render(RenderCallback callback){
            this.onRender = callback;
            return this;
        }

        public Screen build() {
            if(onInit != null) {
                screen.setInitCallback(onInit);
            }
            if(onRender != null){
                screen.setRenderCallback(onRender);
            }
            return screen;
        }
    }
}