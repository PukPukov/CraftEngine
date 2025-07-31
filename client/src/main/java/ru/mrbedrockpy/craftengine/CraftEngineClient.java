package ru.mrbedrockpy.craftengine;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.glfw.GLFW;
import ru.mrbedrockpy.craftengine.event.EventManager;
import ru.mrbedrockpy.craftengine.event.MouseClickEvent;
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
        Window.initialize(new WindowSettings(1280, 720, "CraftEngine Client", false, false));
        Input.initialize();
        context = new DrawContext(Window.getWidth(), Window.getHeight());
        hudRenderer = new HudRenderer(Window.getWidth(), Window.getHeight());
        eventManager.addListener(MouseClickEvent.class, this::onMouseClick);
        Blocks.register();
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
            MouseClickEvent clickEvent = new MouseClickEvent(GLFW.GLFW_MOUSE_BUTTON_LEFT, Input.getX(), Input.getY());
            eventManager.callEvent(clickEvent);
        }
        if (Input.jclicked(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            MouseClickEvent clickEvent = new MouseClickEvent(GLFW.GLFW_MOUSE_BUTTON_RIGHT, Input.getX(), Input.getY());
            eventManager.callEvent(clickEvent);
        }
        
        if(player != null) {
            player.update(deltaTime, tickSystem.getPartialTick(), clientWorld);
        }
    }
    
    private void render() {
        if(clientWorld != null) {
            clientWorld.render();
            hudRenderer.render(context);
        }
        if(currentScreen != null) {
            currentScreen.render(context, (int) Input.getX(), (int) Input.getY(), 0);
        }
    }
    
    public void setScreen(Screen screen) {
        if (currentScreen != null) {
            currentScreen.onClose();
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
        
        Vector3f rayOrigin = player.getCamera().getPosition();
        Vector3f rayDirection = player.getCamera().getFront();
        
        if (event.getButton() == GLFW_MOUSE_BUTTON_LEFT) {
            BlockRaycastResult blockRaycastResult = clientWorld.raycast(rayOrigin, rayDirection, 4.5f);
            if(blockRaycastResult != null){
                clientWorld.setBlock(blockRaycastResult.x, blockRaycastResult.y, blockRaycastResult.z, Blocks.AIR);
            }
        } else if (event.getButton() == GLFW_MOUSE_BUTTON_RIGHT) {
            BlockRaycastResult blockRaycastResult = clientWorld.raycast(rayOrigin, rayDirection, 4.5f);
            if(blockRaycastResult != null && clientWorld.canPlaceBlockAt(blockRaycastResult.getPosition().add(blockRaycastResult.direction.offset()))) {
                Vector3i blockPos = blockRaycastResult.getPosition().add(blockRaycastResult.direction.offset());
                clientWorld.setBlock(blockPos.x, blockPos.y, blockPos.z, Blocks.STONE);
            }
        }
    }
}