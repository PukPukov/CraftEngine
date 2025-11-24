package ru.mrbedrockpy.craftengine.client.gui.screen;

import ru.mrbedrockpy.craftengine.client.CraftEngineClient;
import ru.mrbedrockpy.craftengine.client.gui.screen.layout.VerticalCenterStackLayout;
import ru.mrbedrockpy.craftengine.client.gui.screen.layout.Layout;
import ru.mrbedrockpy.craftengine.client.gui.screen.widget.ButtonWidget;
import ru.mrbedrockpy.renderer.gui.DrawContext;
import ru.mrbedrockpy.renderer.window.Window;


public class MainMenuScreen extends Screen {
    @Override
    public void init() {
        super.init();
        Layout layout = new VerticalCenterStackLayout();

        layout.addWidget("play", new ButtonWidget("Play", 0, 0 ,40,20, b -> CraftEngineClient.INSTANCE.play()));
        layout.addWidget("multiplayer", new ButtonWidget("Multipl", 0, 0, 40, 20, b -> CraftEngineClient.INSTANCE.setScreen(new MultiplayerScreen(CraftEngineClient.INSTANCE.gameClient))));
        layout.addWidget("options", new ButtonWidget("Options", 0, 0, 40, 20, b -> CraftEngineClient.INSTANCE.setScreen(new OptionsScreen())));
        layout.addWidget("exit", new ButtonWidget("Exit", 0, 0, 40, 20, b -> Window.setShouldClose(true)));

        addLayout(layout);
        layout.setOffset(Window.scaledWidth() / 2 - 7, Window.scaledHeight() / 4);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }
}