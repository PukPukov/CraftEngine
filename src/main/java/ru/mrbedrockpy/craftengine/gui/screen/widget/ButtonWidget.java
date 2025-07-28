package ru.mrbedrockpy.craftengine.gui.screen.widget;

import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.CraftEngineClient;
import ru.mrbedrockpy.craftengine.event.MouseClickEvent;
import ru.mrbedrockpy.craftengine.gui.DrawContext;
import ru.mrbedrockpy.craftengine.world.ClientWorld;
import ru.mrbedrockpy.craftengine.world.entity.ClientPlayerEntity;

import java.awt.*;

public class ButtonWidget extends AbstractWidget{
    private final String text;
    
    public ButtonWidget(String text, int x, int y, int width, int height, int zIndex) {
        super(x, y, width, height, zIndex);
        this.text = text;
    }
    
    @Override
    public void onMouseClick(int mouseX, int mouseY, int button) {
        CraftEngineClient.INSTANCE.setPlayer(new ClientPlayerEntity(new Vector3f(5, 5, 2), CraftEngineClient.INSTANCE.getClientWorld()));
        CraftEngineClient.INSTANCE.setClientWorld(new ClientWorld(8, CraftEngineClient.INSTANCE.getPlayer(), CraftEngineClient.INSTANCE.getTickSystem()));
        CraftEngineClient.INSTANCE.setScreen(null);
        
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawRect(x, y, width, height, Color.GREEN);
        context.drawCentredText(text, x + width / 2, y + height / 2);
    }
}