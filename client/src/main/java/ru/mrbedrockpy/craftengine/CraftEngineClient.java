package ru.mrbedrockpy.craftengine;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;
import ru.mrbedrockpy.craftengine.config.ConfigVars;
import ru.mrbedrockpy.craftengine.config.CraftEngineConfiguration;
import ru.mrbedrockpy.craftengine.config.JsonConfig;
import ru.mrbedrockpy.craftengine.event.EventManager;
import ru.mrbedrockpy.craftengine.event.MouseClickEvent;
import ru.mrbedrockpy.craftengine.gui.screen.InventoryScreen;
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

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class CraftEngineClient {
    public static CraftEngineClient INSTANCE = new CraftEngineClient();
    private DrawContext context;
    public HudRenderer hudRenderer;
    public final EventManager eventManager = new EventManager();
    @Getter private final FPSCounter fpsCounter = new FPSCounter();
    @Getter @Setter
    private ClientWorld clientWorld;
    @Getter @Setter
    private ClientPlayerEntity player;
    private Screen currentScreen = null;
    @Getter
    private final TickSystem tickSystem = new TickSystem(20);

    private CraftEngineClient() {}
    
    public void run() {
        this.initialize();
        long lastTime = System.currentTimeMillis();
        while(!Window.isShouldClose()) {
            Input.pullEvents();
            long currentTime = System.currentTimeMillis();
            double deltaTime = (currentTime - lastTime) / 1_000.0;
            lastTime = currentTime;
            this.update(deltaTime);
            Window.clear();
            this.render();
            Window.swapBuffers();
        }
        Window.terminate();
    }
    
    public void initialize() {
        CraftEngineConfiguration.register();
        ConfigVars.update();
        Window.initialize(ConfigVars.WINDOW_SETTINGS);
        Input.initialize();
        context = new DrawContext(Window.scaledWidth(ConfigVars.GUI_SCALE), Window.scaledHeight(ConfigVars.GUI_SCALE));
        eventManager.addListener(MouseClickEvent.class, this::onMouseClick);
        Blocks.register();
        Items.register();
        Registries.freeze();
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
            MouseClickEvent clickEvent = new MouseClickEvent(GLFW.GLFW_MOUSE_BUTTON_LEFT, Input.x(), Input.y());
            eventManager.callEvent(clickEvent);
        }
        if (Input.jclicked(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            MouseClickEvent clickEvent = new MouseClickEvent(GLFW.GLFW_MOUSE_BUTTON_RIGHT, Input.x(), Input.y());
            eventManager.callEvent(clickEvent);
        }
        if (Input.jpressed(GLFW.GLFW_KEY_TAB)){
            setScreen(InventoryScreen.create(player.inventory()));
        }
        
        if(player != null) {
            player.update(deltaTime, tickSystem.partialTick(), clientWorld);
        }
    }
    
    private void render() {
        if(clientWorld != null && player != null) {
            clientWorld.render();
            hudRenderer.render(context, scale((int) Input.x()),  scale((int) Input.y()), (float) tickSystem.partialTick());
        }
        if(currentScreen != null) {
            currentScreen.render(context, scale((int) Input.x()), scale((int) Input.y()), (float) tickSystem.partialTick());
        }
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
        if(currentScreen != null) {
            currentScreen.onMouseClick(event);
            return;
        }
        
        Vector3f rayOrigin = player.camera().position();
        Vector3f rayDirection = player.camera().front();
        
        if (event.button() == GLFW_MOUSE_BUTTON_LEFT) {
            BlockRaycastResult blockRaycastResult = clientWorld.raycast(rayOrigin, rayDirection, 4.5f);
            if(blockRaycastResult != null){
                clientWorld.setBlock(blockRaycastResult.x, blockRaycastResult.y, blockRaycastResult.z, Blocks.AIR);
            }
        } else if (event.button() == GLFW_MOUSE_BUTTON_RIGHT) {
            BlockRaycastResult blockRaycastResult = clientWorld.raycast(rayOrigin, rayDirection, 4.5f);
            if(blockRaycastResult != null && clientWorld.canPlaceBlockAt(blockRaycastResult.position().add(blockRaycastResult.direction.offset()))) {
                Vector3i blockPos = blockRaycastResult.position().add(blockRaycastResult.direction.offset());
                clientWorld.setBlock(blockPos.x, blockPos.y, blockPos.z, Blocks.STONE);
            }
        }
    }

    // Я хз как адекватно сделать масштабирование, поэтому просто делаю так
    private int scale(int value) {
        return (int) (value / (float) Window.width() * Window.scaledWidth(ConfigVars.GUI_SCALE));
    }
}