package ru.mrbedrockpy.craftengine.client;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import ru.mrbedrockpy.craftengine.client.event.client.input.KeyPressEvent;
import ru.mrbedrockpy.craftengine.client.event.client.input.MouseScrollEvent;
import ru.mrbedrockpy.craftengine.client.keybind.KeyBindings;
import ru.mrbedrockpy.craftengine.client.network.ClientPacketHandler;
import ru.mrbedrockpy.craftengine.client.network.auth.GameProfile;
import ru.mrbedrockpy.craftengine.client.serial.WorldIO;
import ru.mrbedrockpy.craftengine.client.world.entity.ClientPlayerEntity;
import ru.mrbedrockpy.craftengine.core.cfg.ConfigVars;
import ru.mrbedrockpy.craftengine.core.cfg.CraftEngineConfiguration;
import ru.mrbedrockpy.craftengine.client.event.EventManager;
import ru.mrbedrockpy.craftengine.client.event.client.input.MouseClickEvent;
import ru.mrbedrockpy.craftengine.client.gui.screen.InventoryScreen;
import ru.mrbedrockpy.craftengine.client.network.GameClient;
import ru.mrbedrockpy.craftengine.core.world.block.Blocks;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketRegistry;
import ru.mrbedrockpy.craftengine.server.network.packet.Packets;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.BlockUpdatePacketS2C;
import ru.mrbedrockpy.craftengine.core.world.item.ItemStack;
import ru.mrbedrockpy.renderer.resource.CompositeResourceManager;
import ru.mrbedrockpy.craftengine.core.world.item.Items;
import ru.mrbedrockpy.renderer.RenderInit;
import ru.mrbedrockpy.renderer.gui.DrawContext;
import ru.mrbedrockpy.craftengine.client.gui.HudRenderer;
import ru.mrbedrockpy.craftengine.client.gui.screen.MainMenuScreen;
import ru.mrbedrockpy.craftengine.client.gui.screen.Screen;
import ru.mrbedrockpy.craftengine.core.registry.Registries;
import ru.mrbedrockpy.craftengine.client.window.*;
import ru.mrbedrockpy.craftengine.client.world.ClientWorld;
import ru.mrbedrockpy.craftengine.server.world.TickSystem;
import ru.mrbedrockpy.renderer.resource.UrlResourceSource;
import ru.mrbedrockpy.renderer.util.graphics.ShaderUtil;
import ru.mrbedrockpy.renderer.window.Input;
import ru.mrbedrockpy.renderer.window.Window;
import ru.mrbedrockpy.renderer.window.WindowSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

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
    private final ClientPacketHandler packetHandler = new ClientPacketHandler(PacketRegistry.INSTANCE);
    public final GameClient gameClient = new GameClient(PacketRegistry.INSTANCE, packetHandler);

    private CraftEngineClient() {
        Packets.register();
        packetHandler.register(BlockUpdatePacketS2C.class, (ctx, pkt) -> clientWorld.setBlock(pkt.pos(), pkt.block()));
        tickSystem.addListener(this::tick);
        CraftEngineConfiguration.register();
        Window.initialize(WindowSettings.DEFAULT);
        RenderInit.CONFIG = ConfigVars.INSTANCE;

        Input.initialize();

        eventManager.addListener(MouseClickEvent.class, this::onMouseClick);
        eventManager.addListener(KeyPressEvent.class, this::onKeyPress);
        eventManager.addListener(MouseScrollEvent.class, this::onMouseScroll);
        Input.onPressAny.add(kc -> eventManager.callEvent(new KeyPressEvent(kc.key())));
        Input.onScrollAny.add(sc -> eventManager.callEvent(new MouseScrollEvent(sc.xoffset(), sc.yoffset())));

        Blocks.register();
        Items.register();
        Registries.freeze();

        resourceManager.push(new UrlResourceSource(this.getClass().getClassLoader(), "."));
        resourceManager.load();

        RenderInit.RESOURCE_MANAGER = resourceManager;
        
        context = new DrawContext(ShaderUtil.load("vertex.glsl", "fragment.glsl"), Window.scaledWidth(), Window.scaledHeight());

        RenderInit.BLOCKS = Registries.BLOCKS;
        KeyBindings.register();
        setScreen(new MainMenuScreen());
    }

    public void run(GameProfile profile) {
        long lastTime = System.currentTimeMillis();
        gameClient.setProfile(profile);
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

        gameClient.close();
        Window.terminate();
    }

    private void update(double deltaTime) {
        fpsCounter.update();
        tickSystem.update(deltaTime);

        if (Input.wasPressed(Input.Layer.UI, GLFW.GLFW_KEY_ESCAPE)) {
            setScreen(null);
        } else if (Input.wasPressed(Input.Layer.GAME, GLFW.GLFW_KEY_ESCAPE)) {
            stop();
            setScreen(new MainMenuScreen());
        }
        if (currentScreen == null && clientWorld == null) {
            setScreen(new MainMenuScreen());
        }

        if (Input.wasPressed(GLFW.GLFW_KEY_F11)) {
            Window.toggleFullscreen();
        }

        if(Input.wasPressed(GLFW.GLFW_KEY_F2)) Window.takeScreenshot();

        if (Input.wasPressed(Input.Layer.GAME, GLFW.GLFW_KEY_TAB)) {
            if (player != null) setScreen(new InventoryScreen(player.getInventory()));
        }

        if (Input.wasClicked(GLFW_MOUSE_BUTTON_LEFT)) {
            MouseClickEvent ev = new MouseClickEvent(Input.currentLayer(), GLFW_MOUSE_BUTTON_LEFT, Input.getX(), Input.getY());
            eventManager.callEvent(ev);
        }
        if (Input.wasClicked(GLFW_MOUSE_BUTTON_RIGHT)) {
            MouseClickEvent ev = new MouseClickEvent(Input.currentLayer(), GLFW_MOUSE_BUTTON_RIGHT, Input.getX(), Input.getY());
            eventManager.callEvent(ev);
        }
        if (KeyBindings.S1.wasPressed()) {
            player.getInventory().setSelectedHotbarSlot(0);
        }
        if (KeyBindings.S2.wasPressed()) {
            player.getInventory().setSelectedHotbarSlot(1);
        }
        if (KeyBindings.S3.wasPressed()) {
            player.getInventory().setSelectedHotbarSlot(2);
        }
        if (KeyBindings.S4.wasPressed()) {
            player.getInventory().setSelectedHotbarSlot(3);
        }
        if (KeyBindings.S5.wasPressed()) {
            player.getInventory().setSelectedHotbarSlot(4);
        }
        if (KeyBindings.S6.wasPressed()) {
            player.getInventory().setSelectedHotbarSlot(5);
        }
        if (KeyBindings.S7.wasPressed()) {
            player.getInventory().setSelectedHotbarSlot(6);
        }
        if (KeyBindings.S8.wasPressed()) {
            player.getInventory().setSelectedHotbarSlot(7);
        }
        if (KeyBindings.S9.wasPressed()) {
            player.getInventory().setSelectedHotbarSlot(8);
        }

        if (player != null) player.update(deltaTime, tickSystem.partialTick());
    }

    private void tick() {
        gameClient.tick();
        if (clientWorld != null) clientWorld.tick();
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

    public void play() {
        player = new ClientPlayerEntity(new Vector3f(0, 0, 0), null);
        try {
//            clientWorld = new ClientWorld(WorldIO.deserialize(CompoundTag.fromBytes(Files.readAllBytes(Paths.get("world.msgpack")))), player);
//            Chunk chunk = new Chunk(new Vector2i());
//            new SimpleChunkGenerator().generate(chunk);
            clientWorld = new ClientWorld(10, player);
        } catch (Exception e) {
            e.printStackTrace();
        }
        player.setPosition(new Vector3f(0, 0, clientWorld.getTopZ(0, 0) + 1));
        setScreen(null);
        player.getInventory().slot(0, new ItemStack(Items.STONE_BLOCK_ITEM));
        player.getInventory().slot(1, new ItemStack(Items.DIRT_BLOCK_ITEM));
        for (int i = 2; i < 9; i++) {
            player.getInventory().slot(i, new ItemStack(Items.GOLDEN_APPLE));
        }
        hudRenderer = new HudRenderer(Window.scaledWidth(), Window.scaledHeight());
//        gameClient.connect("", 8080);
    }

    public void stop() {
        try {
            Files.write(
                    Paths.get("world.msgpack"),
                    WorldIO.serialize(clientWorld).toBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        player = null;
        clientWorld = null;
        hudRenderer = null;
    }

    private void onMouseClick(MouseClickEvent event) {
        if (currentScreen != null && Input.currentLayer() == Input.Layer.UI) {
            currentScreen.onMouseClick(event);
        }
    }

    private void onKeyPress(KeyPressEvent event) {
        if (currentScreen != null && Input.currentLayer() == Input.Layer.UI) {
            currentScreen.onKeyPressed(event);
        }
    }

    private void onMouseScroll(MouseScrollEvent event) {
        if (currentScreen != null && Input.currentLayer() == Input.Layer.UI) {
           currentScreen.onMouseScrolled(event);
        }
        int nextSlot = (player.getInventory().getSelectedHotbarSlot() - (int) ((event.getScrollY() / Math.abs(event.getScrollY())))) % 9;
        player.getInventory().setSelectedHotbarSlot(nextSlot < 0 ? 8 : nextSlot);
    }

    // Я хз как адекватно сделать масштабирование, поэтому просто делаю так
    private int scale(int value) {
        return (int) (value / (float) Window.getWidth() * Window.scaledWidth());
    }
}