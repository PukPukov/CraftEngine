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

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class CraftEngineClient {

    public static final CraftEngineClient INSTANCE = new CraftEngineClient();

    public final EventManager eventManager = new EventManager();
    public HudRenderer hudRenderer;
    public final CompositeResourceManager resourceManager = new CompositeResourceManager();
    private final DrawContext context;
    @Getter
    private final FPSCounter fpsCounter = new FPSCounter();
    @Getter
    @Setter
    private ClientWorld clientWorld;
    @Getter
    @Setter
    private ClientPlayerEntity player;
    @Getter
    private Screen currentScreen = null;
    @Getter
    private final TickSystem tickSystem = new TickSystem(20);

    private CraftEngineClient() {
        CraftEngineConfiguration.register();
        Window.initialize(ConfigVars.INSTANCE.getObject("window.settings", WindowSettings.class));

        Input.initialize();

        context = new DrawContext(Window.scaledWidth(), Window.scaledHeight());

        eventManager.addListener(MouseClickEvent.class, this::onMouseClick);

        Blocks.register();
        Items.register();
        Registries.freeze();

        resourceManager.push(new FileResourceSource(Paths.get("")));
        resourceManager.load();

        RenderInit.RESOURCE_MANAGER = resourceManager;
        RenderInit.BLOCKS = Registries.BLOCKS;

        setScreen(MainMenuScreen.create());
    }

    public void run() {
        long lastTime = System.currentTimeMillis();
        while (!Window.isShouldClose()) {
            Input.pullEvents();
            long now = System.currentTimeMillis();
            double deltaSeconds = (now - lastTime) / 1000.0;
            lastTime = now;
            this.update(deltaSeconds);
            Window.clear();
            this.render();
            this.renderUI();
            Window.swapBuffers();
        }
        Window.terminate();
    }

    private void update(double deltaTime) {
        fpsCounter.update();
        tickSystem.update(deltaTime);

        if (Input.wasPressed(Input.Layer.UI, GLFW.GLFW_KEY_ESCAPE)) {
            setScreen(null);
        } else if (Input.wasPressed(Input.Layer.GAME, GLFW.GLFW_KEY_ESCAPE)) {
            Window.setShouldClose(true);
        }

        if (Input.wasPressed(GLFW.GLFW_KEY_F11)) {
            Window.toggleFullscreen();
        }

        if (Input.wasPressed(Input.Layer.GAME, GLFW.GLFW_KEY_TAB)) {
            if (player != null) setScreen(InventoryScreen.create(player.getInventory()));
        }

        if (Input.wasClicked(GLFW_MOUSE_BUTTON_LEFT)) {
            MouseClickEvent ev = new MouseClickEvent(Input.currentLayer(), GLFW_MOUSE_BUTTON_LEFT, Input.getX(), Input.getY());
            eventManager.callEvent(ev);
        }
        if (Input.wasClicked(GLFW_MOUSE_BUTTON_RIGHT)) {
            MouseClickEvent ev = new MouseClickEvent(Input.currentLayer(), GLFW_MOUSE_BUTTON_RIGHT, Input.getX(), Input.getY());
            eventManager.callEvent(ev);
        }

        if (player != null) player.update(deltaTime, tickSystem.partialTick());
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
        if (currentScreen != null && Input.currentLayer() == Input.Layer.UI) {
            currentScreen.onMouseClick(event);
        }
    }

    // Я хз как адекватно сделать масштабирование, поэтому просто делаю так
    private int scale(int value) {
        return (int) (value / (float) Window.getWidth() * Window.scaledWidth());
    }
}