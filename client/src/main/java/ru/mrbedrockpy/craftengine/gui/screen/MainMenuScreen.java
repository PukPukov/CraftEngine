package ru.mrbedrockpy.craftengine.gui.screen;

import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.CraftEngineClient;
import ru.mrbedrockpy.craftengine.world.ClientWorld;
import ru.mrbedrockpy.craftengine.world.entity.ClientPlayerEntity;
import ru.mrbedrockpy.renderer.window.Window;


public class MainMenuScreen {
    public static Screen create() {
        return UI.create().button("Play", Window.width() / 2 - 50, Window.height() / 2 - 50, 100, 100, button -> {
            CraftEngineClient.INSTANCE.player(new ClientPlayerEntity(new Vector3f(0, 0, 2), null));
            ClientWorld world = new ClientWorld(800, CraftEngineClient.INSTANCE.player(), CraftEngineClient.INSTANCE.tickSystem());
            CraftEngineClient.INSTANCE.clientWorld(world);
            CraftEngineClient.INSTANCE.setScreen(null);
        }).build();
    }
}