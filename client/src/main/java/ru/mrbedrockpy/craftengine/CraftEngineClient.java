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
        long lastTime    = System.currentTimeMillis();

        while (!Window.isShouldClose()) {
            Input.pullEvents();
            long currentTime  = System.currentTimeMillis();
            double deltaMs    = currentTime - lastTime;
            lastTime          = currentTime;
            this.update(deltaMs / 1000.0);
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
        if (Input.jpressed(GLFW.GLFW_KEY_ESCAPE)) {
            Window.setShouldClose(true);
        }
        if (Input.jclicked(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            MouseClickEvent event = new MouseClickEvent(GLFW.GLFW_MOUSE_BUTTON_LEFT, Input.getX(), Input.getY());
            if (currentScreen != null) {
                currentScreen.onMouseClick(event);
                return;
            }

            Vector3f rayOrigin = player.getCamera().getPosition();
            Vector3f rayDirection = player.getCamera().getFront();

            if (event.getButton() == GLFW_MOUSE_BUTTON_LEFT) {
                BlockRaycastResult blockRaycastResult = clientWorld.raycast(rayOrigin, rayDirection, 4.5f);
                if (blockRaycastResult != null) {
                    clientWorld.setBlock(blockRaycastResult.x, blockRaycastResult.y, blockRaycastResult.z, Blocks.AIR);
                }
            } else if (event.getButton() == GLFW_MOUSE_BUTTON_RIGHT) {
                ItemStack selected = player.getInventory().getSelectedStack();
                if(selected != null){
                    selected.item().use(player);
                }
            }

            eventManager.callEvent(event);
        }
        if (Input.jclicked(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            MouseClickEvent event = new MouseClickEvent(GLFW.GLFW_MOUSE_BUTTON_RIGHT, Input.getX(), Input.getY());
            if (currentScreen != null) {
                currentScreen.onMouseClick(event);
                return;
            }

            Vector3f rayOrigin = player.getCamera().getPosition();
            Vector3f rayDirection = player.getCamera().getFront();

            if (event.getButton() == GLFW_MOUSE_BUTTON_LEFT) {
                BlockRaycastResult blockRaycastResult = clientWorld.raycast(rayOrigin, rayDirection, 4.5f);
                if (blockRaycastResult != null) {
                    clientWorld.setBlock(blockRaycastResult.x, blockRaycastResult.y, blockRaycastResult.z, Blocks.AIR);
                }
            } else if (event.getButton() == GLFW_MOUSE_BUTTON_RIGHT) {
                ItemStack selected = player.getInventory().getSelectedStack();
                if(selected != null){
                    selected.item().use(player);
                }
            }

            eventManager.callEvent(event);
        }
        if (Input.jpressed(GLFW.GLFW_KEY_TAB)) {
            setScreen(InventoryScreen.create(player.getInventory()));
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

    private void renderUI(){
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
        }
        this.currentScreen = screen;
        if (screen != null) {
            Input.openGUI();
            screen.init();
        }
    }

    public void onMouseClick(MouseClickEvent event) {
        if (currentScreen != null) {
            currentScreen.onMouseClick(event);
            return;
        }

        Vector3f rayOrigin = player.getCamera().getPosition();
        Vector3f rayDirection = player.getCamera().getFront();

        if (event.getButton() == GLFW_MOUSE_BUTTON_LEFT) {
            BlockRaycastResult blockRaycastResult = clientWorld.raycast(rayOrigin, rayDirection, 4.5f);
            if (blockRaycastResult != null) {
                clientWorld.setBlock(blockRaycastResult.x, blockRaycastResult.y, blockRaycastResult.z, Blocks.AIR);
            }
        } else if (event.getButton() == GLFW_MOUSE_BUTTON_RIGHT) {
            ItemStack selected = player.getInventory().getSelectedStack();
            if(selected != null){
                selected.item().use(player);
            }
        }
    }

    // Я хз как адекватно сделать масштабирование, поэтому просто делаю так
    private int scale(int value) {
        return (int) (value / (float) Window.getWidth() * Window.scaledWidth());
    }
}