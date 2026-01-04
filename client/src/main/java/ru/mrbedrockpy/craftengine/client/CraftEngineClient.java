package ru.mrbedrockpy.craftengine.client;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.client.event.EventManager;
import ru.mrbedrockpy.craftengine.client.event.client.CharTypeEvent;
import ru.mrbedrockpy.craftengine.client.event.client.KeyPressEvent;
import ru.mrbedrockpy.craftengine.client.event.client.MouseClickEvent;
import ru.mrbedrockpy.craftengine.client.event.client.MouseScrollEvent;
import ru.mrbedrockpy.craftengine.client.gui.HudRenderer;
import ru.mrbedrockpy.craftengine.client.gui.screen.MainMenuScreen;
import ru.mrbedrockpy.craftengine.client.gui.screen.Screen;
import ru.mrbedrockpy.craftengine.client.keybind.KeyBindings;
import ru.mrbedrockpy.craftengine.client.network.ClientPacketHandler;
import ru.mrbedrockpy.craftengine.client.network.GameClient;
import ru.mrbedrockpy.craftengine.client.network.auth.GameProfile;
import ru.mrbedrockpy.craftengine.client.serial.CompoundTag;
import ru.mrbedrockpy.craftengine.client.serial.WorldIO;
import ru.mrbedrockpy.craftengine.client.world.ClientPlayerController;
import ru.mrbedrockpy.craftengine.client.world.ClientWorld;
import ru.mrbedrockpy.craftengine.client.world.entity.ClientPlayerEntity;
import ru.mrbedrockpy.craftengine.core.registry.Registries;
import ru.mrbedrockpy.craftengine.core.util.config.ConfigManager;
import ru.mrbedrockpy.craftengine.core.util.config.CraftEngineConfig;
import ru.mrbedrockpy.craftengine.core.world.block.Blocks;
import ru.mrbedrockpy.craftengine.core.world.item.ItemStack;
import ru.mrbedrockpy.craftengine.core.world.item.Items;
import ru.mrbedrockpy.craftengine.server.network.packet.PacketRegistry;
import ru.mrbedrockpy.craftengine.server.network.packet.Packets;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.BlockUpdatePacketS2C;
import ru.mrbedrockpy.craftengine.server.network.packet.custom.ChatMessagePacketS2C;
import ru.mrbedrockpy.craftengine.server.util.chat.ChatManager;
import ru.mrbedrockpy.craftengine.server.world.TickSystem;
import ru.mrbedrockpy.renderer.RenderInit;
import ru.mrbedrockpy.renderer.gui.DrawContext;
import ru.mrbedrockpy.renderer.resource.CompositeResourceManager;
import ru.mrbedrockpy.renderer.resource.UrlResourceSource;
import ru.mrbedrockpy.renderer.util.graphics.ShaderUtil;
import ru.mrbedrockpy.renderer.window.Input;
import ru.mrbedrockpy.renderer.window.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;

public class CraftEngineClient {

    public static final CraftEngineClient INSTANCE = new CraftEngineClient();

    // модули
    private @Getter final   EventManager             eventManager      = new EventManager();
    private @Getter         HudRenderer              hudRenderer;
    private @Getter final   CompositeResourceManager resourceManager   = new CompositeResourceManager();
    private         final   DrawContext              context;
    private @Getter final   ChatManager              chatManager       = new ChatManager();
    private @Getter final   TickSystem               tickSystem        = new TickSystem(20);
    private         final   ClientPacketHandler      packetHandler     = new ClientPacketHandler(PacketRegistry.INSTANCE);
    private @Getter final   GameClient               gameClient        = new GameClient(PacketRegistry.INSTANCE, packetHandler);
    private @Getter final   ConfigManager            configManager;
    
    // данные
    private @Getter @Setter ClientWorld              clientWorld;
    private @Getter @Setter ClientPlayerEntity       player;
    private @Getter @Setter ClientPlayerController   playerController;
    private @Getter         Screen                   currentScreen;
    private @Getter         double                   delta;

    private CraftEngineClient() {
        Packets.register();
        packetHandler.register(BlockUpdatePacketS2C.class, (ctx, pkt) -> clientWorld.setBlock(pkt.pos(), pkt.block()));
        packetHandler.register(ChatMessagePacketS2C.class, (ctx, pkt) -> chatManager.onMessage(pkt.name(), pkt.message()));
        tickSystem.addListener(this::tick);
        this.configManager = ConfigManager.builder()
                .setParentFolder(new File("."))
                .setConfigs(CraftEngineConfig.class)
                .build();

        gameClient.setProfile(new GameProfile(CraftEngineConfig.Network.NAME));
        Window.initialize(CraftEngineConfig.WINDOW);
        Input.initialize();

        eventManager.addListener(MouseClickEvent.class, this::onMouseClick);
        eventManager.addListener(KeyPressEvent.class, this::onKeyPress);
        eventManager.addListener(MouseScrollEvent.class, this::onMouseScroll);
        eventManager.addListener(CharTypeEvent.class, this::onCharTyped);

        Input.onPressAny.add(kc -> eventManager.callEvent(new KeyPressEvent(kc.key(), kc.scancode(), kc.inputAction(), kc.mods())));
        Input.onScrollAny.add(sc -> eventManager.callEvent(new MouseScrollEvent(sc.xoffset(), sc.yoffset())));
        Input.onCharAny.add(sc -> eventManager.callEvent(new CharTypeEvent(sc.c(), sc.mods())));

        Blocks.register();
        Items.register();
        Registries.freeze();

        resourceManager.push(new UrlResourceSource(this.getClass().getClassLoader(), ""));
        resourceManager.load();

        RenderInit.RESOURCE_MANAGER = resourceManager;

        context = new DrawContext(ShaderUtil.load("vertex.glsl", "fragment.glsl"));

        RenderInit.BLOCKS = Registries.BLOCKS;
        KeyBindings.register();
        setScreen(new MainMenuScreen());
    }

    public void run() {
        long lastTime = System.currentTimeMillis();
        while (!Window.isShouldClose()) {
            Input.pullEvents();
            long now = System.currentTimeMillis();
            delta = (now - lastTime) / 1000.0;
            lastTime = now;
            this.update();
            Window.clear();
            this.render();
            this.renderUI();
            Window.swapBuffers();
        }
        this.configManager.saveConfigs();
        gameClient.close();
        Window.terminate();
    }

    private void update() {
        tickSystem.update(delta);
        if (Input.wasClicked(GLFW_MOUSE_BUTTON_LEFT)) {
            MouseClickEvent ev = new MouseClickEvent(Input.getCurrentLayer(), GLFW_MOUSE_BUTTON_LEFT, Input.getX(), Input.getY());
            eventManager.callEvent(ev);
        }
        if (Input.wasClicked(GLFW_MOUSE_BUTTON_RIGHT)) {
            MouseClickEvent ev = new MouseClickEvent(Input.getCurrentLayer(), GLFW_MOUSE_BUTTON_RIGHT, Input.getX(), Input.getY());
            eventManager.callEvent(ev);
        }
        if (playerController != null) playerController.update(delta, tickSystem.partialTick());
    }

    private void tick() {
        gameClient.tick();
        if (currentScreen != null) currentScreen.tick();
        if (clientWorld != null) clientWorld.tick();
    }

    private void render() {
        if (clientWorld != null && player != null) {
            clientWorld.render();
        }
    }

    private void renderUI() {
        context.enableGL();
        if (clientWorld != null && player != null) hudRenderer.render(context, scale(Input.getX()), scale(Input.getY()), (float) delta);
        if (currentScreen != null) currentScreen.render(context, scale(Input.getX()), scale(Input.getY()), (float) delta);
        context.disableGL();
    }

    public void setScreen(@Nullable Screen screen) {
        if (currentScreen != null) {
            currentScreen.close();
            currentScreen = null;
            if (Input.getCurrentLayer() == Input.Layer.UI) {
                Input.setLayer(null);
            }
        }
        this.currentScreen = screen;
        if (screen != null) {
            Input.setLayer(Input.Layer.UI);
            screen.init();
        }
    }

    // TODO: вынести вход и выход из мира
    public void play() {
        player = new ClientPlayerEntity(new Vector3f(0, 0, 0), null);
        playerController = new ClientPlayerController(player);
        try {
            clientWorld = new ClientWorld(WorldIO.deserialize(CompoundTag.fromBytes(Files.readAllBytes(Paths.get("world.msgpack")))), player);
        } catch (Exception e) {
            clientWorld = new ClientWorld(10, player);
        }
        player.setPosition(new Vector3f(0, clientWorld.getTopY(0, 0) + 1, 0));
        setScreen(null);
        player.getInventory().setStack(0, new ItemStack(Items.STONE_BLOCK_ITEM));
        player.getInventory().setStack(1, new ItemStack(Items.DIRT_BLOCK_ITEM));
        for (int i = 2; i < 37; i++) {
            player.getInventory().setStack(i, Items.GOLDEN_APPLE.getDefStack());
        }
        for(int i = 0; i < 4; i++){
            player.getInventory().setArmor(i, Items.GOLDEN_APPLE.getDefStack());
        }
        hudRenderer = new HudRenderer(Window.scaledWidth(), Window.scaledHeight());
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
        playerController = null;
    }

    private void onMouseClick(MouseClickEvent event) {
        if (currentScreen != null && Input.getCurrentLayer() == Input.Layer.UI) {
            currentScreen.onMouseClick(event);
        }
    }

    private void onKeyPress(KeyPressEvent event) {
        if (currentScreen != null && Input.getCurrentLayer() == Input.Layer.UI) {
            currentScreen.onKeyPressed(event);
        }
    }

    private void onCharTyped(CharTypeEvent event) {
        if (currentScreen != null && Input.getCurrentLayer() == Input.Layer.UI) {
            currentScreen.charType(event);
        }
    }

    private void onMouseScroll(MouseScrollEvent event) {
        if (currentScreen != null && Input.getCurrentLayer() == Input.Layer.UI) {
           currentScreen.onMouseScrolled(event);
        } else {
            int nextSlot = (player.getInventory().getSelectedHotbarSlot() - (int) ((event.getScrollY() / Math.abs(event.getScrollY())))) % 9;
            player.getInventory().setSelectedHotbarSlot(nextSlot < 0 ? 8 : nextSlot);
        }
    }

    private int scale(double value) {
        return (int) (value / (float) Window.getWidth() * Window.scaledWidth());
    }
}