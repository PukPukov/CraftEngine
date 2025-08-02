package ru.mrbedrockpy.craftengine.gui.screen;

import ru.mrbedrockpy.craftengine.gui.screen.callback.RenderCallback;
import ru.mrbedrockpy.craftengine.gui.screen.widget.AbstractWidget;
import ru.mrbedrockpy.craftengine.gui.screen.widget.ButtonWidget;
import ru.mrbedrockpy.craftengine.gui.screen.widget.SlotWidget;
import ru.mrbedrockpy.craftengine.world.inventory.PlayerInventory;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;

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

        public Builder slot(Consumer<ButtonWidget> onClick){
            return slot(0, 0, onClick);
        }

        public Builder slot(int x, int y, Consumer<ButtonWidget> onClick) {
            screen.addWidget(new ButtonWidget("Slot", x, y, 18, 18, onClick));
            return this;
        }

        public Builder grid(
                List<? extends AbstractWidget> widgets,
                int columns,
                int startX,
                int startY,
                int spacingX,
                int spacingY
        ) {
            for (int i = 0; i < widgets.size(); i++) {
                int row = i / columns;
                int col = i % columns;

                AbstractWidget widget = widgets.get(i);
                int x = startX + col * (widget.width() + spacingX);
                int y = startY + row * (widget.height() + spacingY);

                widget.setPosition(x, y);
                screen.addWidget(widget);
            }
            return this;
        }

        public Builder grid(IntFunction<? extends AbstractWidget> factory,
                            int columns,
                            int rows,
                            int startX,
                            int startY,
                            int spacingX,
                            int spacingY
        ){
            int index = 0;
            for(int i = 0; i < columns; i++) {
                for(int j = 0; j < rows; j++) {
                    AbstractWidget w = factory.apply(index);
                    int x = startX + j * (w.width() + spacingX);
                    int y = startY + i * (w.height() + spacingY);
                    w.setPosition(x, y);
                    screen.addWidget(w);
                    index++;
                }
            }
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

        public Screen b() {
            if(onInit != null) {
                screen.setInitCallback(onInit);
            }
            if(onRender != null){
                screen.setRenderCallback(onRender);
            }
            return screen;
        }
    }

    public static SlotWidget slot(PlayerInventory inv, int slotIndex,
                                 Consumer<SlotWidget> onClick) {
        return new SlotWidget(
                0, 0,
                () -> inv.stack(slotIndex), // getter
                s  -> inv.stack(slotIndex, s), // setter
                onClick);
    }
}