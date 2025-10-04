package ru.mrbedrockpy.craftengine.client.gui.screen;

import ru.mrbedrockpy.craftengine.client.gui.screen.layout.Layout;
import ru.mrbedrockpy.craftengine.client.gui.screen.layout.VerticalCenterStackLayout;
import ru.mrbedrockpy.craftengine.client.gui.screen.widget.BindingWidget;
import ru.mrbedrockpy.craftengine.client.keybind.KeyBind;
import ru.mrbedrockpy.craftengine.client.keybind.KeyBindingsHelper;
import ru.mrbedrockpy.renderer.window.Window;

public class OptionsScreen extends Screen {
    public OptionsScreen(){
    }

    @Override
    public void init() {
        super.init();
        Layout binds = new VerticalCenterStackLayout();

        for(KeyBind bind : KeyBindingsHelper.all()){
            binds.addWidget(bind.getName(), new BindingWidget(bind, 0, 0, 40, 20, c -> {}));
        }
        addLayout(binds);
        binds.setOffset(Window.scaledWidth() / 2, Window.scaledHeight() / 4);
    }
}
