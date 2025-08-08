package ru.mrbedrockpy.craftengine.gui.screen;

import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.CraftEngineClient;
import ru.mrbedrockpy.craftengine.config.ConfigVars;
import ru.mrbedrockpy.craftengine.config.CraftEngineConfiguration;
import ru.mrbedrockpy.craftengine.gui.HudRenderer;
import ru.mrbedrockpy.craftengine.world.ClientWorld;
import ru.mrbedrockpy.craftengine.world.entity.ClientPlayerEntity;
import ru.mrbedrockpy.craftengine.world.item.ItemStack;
import ru.mrbedrockpy.craftengine.world.item.Items;
import ru.mrbedrockpy.renderer.window.Window;


public class MainMenuScreen {
    
    public static Screen create() {
        return UI.create().button(
            "Play", Window.scaledWidth(ConfigVars.GUI_SCALE) / 2 - 50, Window.scaledHeight(ConfigVars.GUI_SCALE) / 2 - 50, 50, 50, button -> {
                ClientPlayerEntity player = new ClientPlayerEntity(new Vector3f(0, 0, 18), null);
                CraftEngineClient.INSTANCE.player(player);
                ClientWorld world = new ClientWorld(100, CraftEngineClient.INSTANCE.player(), CraftEngineClient.INSTANCE.tickSystem());
                CraftEngineClient.INSTANCE.clientWorld(world);
                CraftEngineClient.INSTANCE.setScreen(null);
                player.inventory().slot(0, new ItemStack(Items.STONE_BLOCK_ITEM));
                for (int i = 1; i < 9; i++) {
                    player.inventory().slot(i, new ItemStack(Items.GOLDEN_APPLE));
                }
                CraftEngineClient.INSTANCE.hudRenderer = new HudRenderer(Window.scaledWidth(ConfigVars.GUI_SCALE), Window.scaledHeight(ConfigVars.GUI_SCALE));
            }
        ).b();
    }
    
}