package ru.mrbedrockpy.craftengine;

import lombok.Getter;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import ru.mrbedrockpy.craftengine.event.EventManager;
import ru.mrbedrockpy.craftengine.event.MouseClickEvent;
import ru.mrbedrockpy.craftengine.graphics.Texture;
import ru.mrbedrockpy.craftengine.gui.DrawContext;
import ru.mrbedrockpy.craftengine.gui.HudRenderer;
import ru.mrbedrockpy.craftengine.registry.Registries;
import ru.mrbedrockpy.craftengine.window.*;
import ru.mrbedrockpy.craftengine.world.ClientWorld;
import ru.mrbedrockpy.craftengine.world.TickSystem;
import ru.mrbedrockpy.craftengine.world.block.Block;
import ru.mrbedrockpy.craftengine.world.entity.ClientPlayerEntity;

public class CraftEngineClient {
    public static CraftEngineClient INSTANCE = new CraftEngineClient();
    private DrawContext context;
    public HudRenderer hudRenderer;
    public final EventManager eventManager = new EventManager();
    @Getter private final FPSCounter fpsCounter = new FPSCounter();
    private ClientWorld clientWorld;
    @Getter private ClientPlayerEntity player;
    private final TickSystem tickSystem = new TickSystem(20);

    private CraftEngineClient() {}

    public void run() {
        this.initialize();
        long lastTime = System.nanoTime();
        while(!Window.isShouldClose()) {
            Input.pullEvents();
            long currentTime = System.nanoTime();
            float deltaTime = (currentTime - lastTime) / 1_000_000_000.0f;
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
        player = new ClientPlayerEntity(new Vector3f(5, 1, 5), clientWorld);
        clientWorld = new ClientWorld(8, player, tickSystem);
        player.setWorld(clientWorld);
        eventManager.addListener(MouseClickEvent.class, player::onMouseClick);
        context = new DrawContext(Window.getWidth(), Window.getHeight());
        hudRenderer = new HudRenderer(Window.getWidth(), Window.getHeight());
        hudRenderer.texture = Texture.load("cursor.png");
        hudRenderer.hudTexture = Texture.load("hotbar.png");
        Registries.BLOCKS.register("dirt", new Block(true));
        Registries.BLOCKS.register("stone", new Block(true));
        Registries.freeze();
    }

    private void update(float deltaTime) {
        fpsCounter.update();
        tickSystem.update(deltaTime);
        if(Input.jpressed(GLFW.GLFW_KEY_ESCAPE)) {
            Window.setShouldClose(true);
        }
        if(Input.jclicked(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
            MouseClickEvent clickEvent = new MouseClickEvent(GLFW.GLFW_MOUSE_BUTTON_LEFT, Input.getX(), Input.getY());
            eventManager.callEvent(clickEvent);
        }
        if(Input.jclicked(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
            MouseClickEvent clickEvent = new MouseClickEvent(GLFW.GLFW_MOUSE_BUTTON_RIGHT, Input.getX(), Input.getY());
            eventManager.callEvent(clickEvent);
        }
        player.update(deltaTime,tickSystem.getPartialTick() , clientWorld);
    }

    private void render() {
        clientWorld.render();
        hudRenderer.render(context);
    }
}
