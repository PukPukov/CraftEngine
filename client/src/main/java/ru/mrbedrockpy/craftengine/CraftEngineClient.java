package ru.mrbedrockpy.craftengine;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import ru.mrbedrockpy.craftengine.config.ConfigVars;
import ru.mrbedrockpy.craftengine.config.CraftEngineConfiguration;
import ru.mrbedrockpy.craftengine.event.EventManager;
import ru.mrbedrockpy.craftengine.event.MouseClickEvent;
import ru.mrbedrockpy.craftengine.gui.screen.InventoryScreen;
import ru.mrbedrockpy.renderer.resource.CompositeResourceManager;
import ru.mrbedrockpy.renderer.resource.FileResourceSource;
import ru.mrbedrockpy.craftengine.world.item.ItemStack;
import ru.mrbedrockpy.craftengine.world.item.Items;
import ru.mrbedrockpy.renderer.RenderInit;
import ru.mrbedrockpy.renderer.gui.DrawContext;
import ru.mrbedrockpy.craftengine.gui.HudRenderer;
import ru.mrbedrockpy.craftengine.gui.screen.MainMenuScreen;
import ru.mrbedrockpy.craftengine.gui.screen.Screen;
import ru.mrbedrockpy.craftengine.registry.Registries;
import ru.mrbedrockpy.craftengine.window.*;
import ru.mrbedrockpy.craftengine.world.ClientWorld;
import ru.mrbedrockpy.craftengine.world.TickSystem;
import ru.mrbedrockpy.craftengine.world.block.Blocks;
import ru.mrbedrockpy.craftengine.world.entity.ClientPlayerEntity;
import ru.mrbedrockpy.renderer.window.Input;
import ru.mrbedrockpy.renderer.window.Window;
import ru.mrbedrockpy.renderer.window.WindowSettings;
import ru.mrbedrockpy.renderer.world.raycast.BlockRaycastResult;

import java.nio.file.Paths;

import static org.lwjgl.glfw.GLFW.*;

public class CraftEngineClient {

    public static CraftEngineClient INSTANCE = new CraftEngineClient();

    public final EventManager eventManager = new EventManager();
    public       HudRenderer hudRenderer;
    public static final CompositeResourceManager RESOURCE_MANAGER = new CompositeResourceManager();
    private                 DrawContext context;
    private @Getter final   FPSCounter fpsCounter = new FPSCounter();
    private @Getter @Setter ClientWorld clientWorld;
    private @Getter @Setter ClientPlayerEntity player;
    private @Getter         Screen currentScreen = null;
    private @Getter final   TickSystem tickSystem = new TickSystem(20);

    private CraftEngineClient() {}

    public void run() {
        this.initialize();
        long lastTime = System.currentTimeMillis();

        while (!Window.isShouldClose()) {
            Input.pullEvents();
            long now     = System.currentTimeMillis();
            double dtSec = (now - lastTime) / 1000.0;
            lastTime     = now;

            this.update(dtSec);
            Window.clear();
            this.render();
            this.renderUI();
            Window.swapBuffers();
        }
        Window.terminate();
    }

    public void initialize() {
        CraftEngineConfiguration.register();
        Window.initialize(ConfigVars.INSTANCE.getObject("window.settings", WindowSettings.class));

        Input.initialize();

        context = new DrawContext(Window.scaledWidth(), Window.scaledHeight());

        eventManager.addListener(MouseClickEvent.class, this::onMouseClick);

        Blocks.register();
        Items.register();
        Registries.freeze();

        RESOURCE_MANAGER.push(new FileResourceSource(Paths.get("")));
        RESOURCE_MANAGER.load();

        RenderInit.RESOURCE_MANAGER = RESOURCE_MANAGER;
        RenderInit.BLOCKS = Registries.BLOCKS;

        setScreen(MainMenuScreen.create());
    }

    private void update(double deltaTime) {
        fpsCounter.update();
        tickSystem.update(deltaTime);

        if (Input.wasPressed(Input.Layer.UI, GLFW.GLFW_KEY_ESCAPE)) {
            setScreen(null);
        } else if (Input.wasPressed(Input.Layer.GAME, GLFW.GLFW_KEY_ESCAPE)) {
            Window.setShouldClose(true);
        }

        if (Input.wasPressed(Input.Layer.UI, GLFW.GLFW_KEY_F11) || Input.wasPressed(Input.Layer.GAME, GLFW.GLFW_KEY_F11)) {
            Window.toggleFullscreen();
        }

        if (Input.wasPressed(Input.Layer.GAME, GLFW.GLFW_KEY_TAB)
                || Input.wasPressed(Input.Layer.UI, GLFW.GLFW_KEY_TAB)) {
            if (player != null) setScreen(InventoryScreen.create(player.getInventory()));
        }

        if (Input.wasPressed(Input.Layer.GAME, GLFW.GLFW_KEY_F2)
                || Input.wasPressed(Input.Layer.UI, GLFW.GLFW_KEY_F2)) {
            Window.takeScreenshot();
        }

        if(Input.wasMouseClickedThisFrame(GLFW_MOUSE_BUTTON_LEFT)){
            MouseClickEvent ev = new MouseClickEvent(Input.currentLayer(), GLFW_MOUSE_BUTTON_LEFT, Input.getX(), Input.getY());
            eventManager.callEvent(ev);
        }
        if(Input.wasMouseClickedThisFrame(GLFW_MOUSE_BUTTON_RIGHT)){
            MouseClickEvent ev = new MouseClickEvent(Input.currentLayer(), GLFW_MOUSE_BUTTON_RIGHT, Input.getX(), Input.getY());
            eventManager.callEvent(ev);
        }

        if (player != null) {
            player.update(deltaTime, tickSystem.partialTick(), clientWorld);
        }
    }

    private void render() {
        if (clientWorld != null && player != null) {
            clientWorld.render();
        }
    }

    private void renderUI() {
        context.enableGL();
        if (clientWorld != null && player != null) {
            hudRenderer.render(context, scale((int) Input.getX()), scale((int) Input.getY()), (float) tickSystem.partialTick());
        }
        if (currentScreen != null) {
            currentScreen.render(context, scale((int) Input.getX()), scale((int) Input.getY()), (float) tickSystem.partialTick());
        }
        context.disableGL();
    }

    public void setScreen(@Nullable Screen screen) {
        if (currentScreen != null) {
            currentScreen.close();
            currentScreen = null;
            if (Input.currentLayer() == Input.Layer.UI) {
                Input.popLayer();
            }
        }
        this.currentScreen = screen;
        if (screen != null) {
            if (Input.currentLayer() != Input.Layer.UI) {
                Input.pushLayer(Input.Layer.UI);
            }
            screen.init();
        }
    }

    public void onMouseClick(MouseClickEvent event) {
        if(event.getLayer() == Input.Layer.UI) {
            if (currentScreen != null) {
                currentScreen.onMouseClick(event);
            }
        }
    }

    // Я хз как адекватно сделать масштабирование, поэтому просто делаю так
    private int scale(int value) {
        return (int) (value / (float) Window.getWidth() * Window.scaledWidth());
    }
}