package ru.mrbedrockpy.craftengine.client.gui.screen;

import ru.mrbedrockpy.craftengine.client.event.client.input.MouseScrollEvent;
import ru.mrbedrockpy.craftengine.client.gui.screen.layout.Layout;
import ru.mrbedrockpy.craftengine.client.gui.screen.layout.VerticalCenterStackLayout;
import ru.mrbedrockpy.craftengine.client.gui.screen.widget.BindingWidget;
import ru.mrbedrockpy.craftengine.client.keybind.KeyBind;
import ru.mrbedrockpy.craftengine.client.keybind.KeyBindingsHelper;
import ru.mrbedrockpy.renderer.window.Window;

public class OptionsScreen extends Screen {
    private Layout layout;
    private double scroll;
    public OptionsScreen(){
    }

    @Override
    public void init() {
        super.init();
        layout = new VerticalCenterStackLayout();

        for(KeyBind bind : KeyBindingsHelper.all()){
            layout.addWidget(bind.getName(), new BindingWidget(bind, 0, 0, 40, 20, c -> {}));
        }
        addLayout(layout);
        layout.setOffset(Window.scaledWidth() / 2, Window.scaledHeight() / 4);
    }

    @Override
    public void onMouseScrolled(MouseScrollEvent event) {
        super.onMouseScrolled(event);
        scroll += event.getScrollY() * 5;
        scroll = Math.min(0, scroll);
        layout.setOffset(Window.scaledWidth() / 2, (int) ((double) Window.scaledHeight() / 4 + scroll));
    }
}
