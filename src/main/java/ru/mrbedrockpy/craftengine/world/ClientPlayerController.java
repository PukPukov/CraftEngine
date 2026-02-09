package ru.mrbedrockpy.craftengine.world;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import ru.mrbedrockpy.craftengine.CraftEngineClient;
import ru.mrbedrockpy.craftengine.event.EventManager;
import ru.mrbedrockpy.craftengine.gui.screen.InventoryScreen;
import ru.mrbedrockpy.craftengine.gui.screen.MainMenuScreen;
import ru.mrbedrockpy.craftengine.keybind.KeyBindings;
import ru.mrbedrockpy.craftengine.world.entity.ClientPlayerEntity;
import ru.mrbedrockpy.craftengine.renderer.window.Input;
import ru.mrbedrockpy.craftengine.renderer.window.Window;

@Getter
@AllArgsConstructor
public class ClientPlayerController {

    private final ClientPlayerEntity player;

    public void update(double deltaTime, double partialTick) {
        handleInput();
        player.update(deltaTime, partialTick);
    }

    private void handleInput() {
        CraftEngineClient client = CraftEngineClient.INSTANCE;
        EventManager eventManager = client.getEventManager();
        if (Input.wasPressed(Input.Layer.UI, GLFW.GLFW_KEY_ESCAPE)) client.setScreen(null);
        else if (Input.wasPressed(Input.Layer.GAME, GLFW.GLFW_KEY_ESCAPE)) {
            client.stop();
            client.setScreen(new MainMenuScreen());
        }
        if (client.getCurrentScreen() == null && client.getClientWorld() == null) client.setScreen(new MainMenuScreen());

        if (Input.wasPressed(GLFW.GLFW_KEY_F11)) Window.toggleFullscreen();
        if(Input.wasPressed(GLFW.GLFW_KEY_F2)) Window.takeScreenshot();
        if(Input.wasPressed(GLFW.GLFW_KEY_F3)) client.getHudRenderer().toggleDebug();

        if (player == null) return;
        if (KeyBindings.OPEN_INVENTORY.wasPressed()) client.setScreen(new InventoryScreen(player.getInventory()));
        if (KeyBindings.S1.wasPressed()) player.getInventory().setSelectedHotbarSlot(0);
        if (KeyBindings.S2.wasPressed()) player.getInventory().setSelectedHotbarSlot(1);
        if (KeyBindings.S3.wasPressed()) player.getInventory().setSelectedHotbarSlot(2);
        if (KeyBindings.S4.wasPressed()) player.getInventory().setSelectedHotbarSlot(3);
        if (KeyBindings.S5.wasPressed()) player.getInventory().setSelectedHotbarSlot(4);
        if (KeyBindings.S6.wasPressed()) player.getInventory().setSelectedHotbarSlot(5);
        if (KeyBindings.S7.wasPressed()) player.getInventory().setSelectedHotbarSlot(6);
        if (KeyBindings.S8.wasPressed()) player.getInventory().setSelectedHotbarSlot(7);
        if (KeyBindings.S9.wasPressed()) player.getInventory().setSelectedHotbarSlot(8);
    }

}
