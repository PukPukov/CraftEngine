package ru.mrbedrockpy.craftengine.client.world;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lwjgl.glfw.GLFW;
import ru.mrbedrockpy.craftengine.client.CraftEngineClient;
import ru.mrbedrockpy.craftengine.client.event.EventManager;
import ru.mrbedrockpy.craftengine.client.event.client.MouseClickEvent;
import ru.mrbedrockpy.craftengine.client.gui.screen.ChatScreen;
import ru.mrbedrockpy.craftengine.client.gui.screen.InventoryScreen;
import ru.mrbedrockpy.craftengine.client.gui.screen.MainMenuScreen;
import ru.mrbedrockpy.craftengine.client.keybind.KeyBindings;
import ru.mrbedrockpy.craftengine.client.world.entity.ClientPlayerEntity;
import ru.mrbedrockpy.renderer.window.Input;
import ru.mrbedrockpy.renderer.window.Window;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

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
            quitFromWorld();
            client.setScreen(new MainMenuScreen());
        }
        if (client.getCurrentScreen() == null && client.getClientWorld() == null) client.setScreen(new MainMenuScreen());

        if (Input.wasPressed(GLFW.GLFW_KEY_F11)) Window.toggleFullscreen();
        if(Input.wasPressed(GLFW.GLFW_KEY_F2)) Window.takeScreenshot();
        if(Input.wasPressed(GLFW.GLFW_KEY_F3)) client.getHudRenderer().toggleDebug();

        if (player != null && KeyBindings.OPEN_INVENTORY.wasPressed()) client.setScreen(new InventoryScreen(player.getInventory()));
        if (KeyBindings.CHAT.wasPressed()) client.setScreen(new ChatScreen());
        if (Input.wasClicked(GLFW_MOUSE_BUTTON_LEFT)) {
            MouseClickEvent ev = new MouseClickEvent(Input.currentLayer(), GLFW_MOUSE_BUTTON_LEFT, Input.getX(), Input.getY());
            eventManager.callEvent(ev);
        }
        if (Input.wasClicked(GLFW_MOUSE_BUTTON_RIGHT)) {
            MouseClickEvent ev = new MouseClickEvent(Input.currentLayer(), GLFW_MOUSE_BUTTON_RIGHT, Input.getX(), Input.getY());
            eventManager.callEvent(ev);
        }
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

    public void quitFromWorld() {

    }
}
