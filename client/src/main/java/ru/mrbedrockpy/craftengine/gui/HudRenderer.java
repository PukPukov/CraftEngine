package ru.mrbedrockpy.craftengine.gui;

import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.CraftEngineClient;
import ru.mrbedrockpy.craftengine.gui.screen.InventoryScreen;
import ru.mrbedrockpy.craftengine.gui.screen.UI;
import ru.mrbedrockpy.craftengine.gui.screen.widget.SlotWidget;
import ru.mrbedrockpy.craftengine.world.entity.ClientPlayerEntity;
import ru.mrbedrockpy.renderer.graphics.Texture;
import ru.mrbedrockpy.renderer.gui.DrawContext;

import java.text.DecimalFormat;

public class HudRenderer {
    
    private final DecimalFormat decimalFormat = new DecimalFormat();
    
    public int width;
    public int height;
    public SlotWidget[] hotbarSlots = new SlotWidget[9];
    
    public HudRenderer(int width, int height) {
        this.decimalFormat.setMaximumFractionDigits(3);
        this.decimalFormat.setMinimumFractionDigits(3);
        this.width = width;
        this.height = height;
        for (int i = 0; i < hotbarSlots.length; i++) {
            hotbarSlots[i] = UI.slot(CraftEngineClient.INSTANCE.player().inventory(), i, InventoryScreen::slotClick);
            hotbarSlots[i].setPosition(width / 2 + i * 20 - hudTexture.width() / 2 + 2, height - hudTexture.height() + 2);
        }
    }
    
    public Texture texture = Texture.load("cursor.png"), hudTexture = Texture.load("hotbar.png");
    
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTexture(width / 2, height / 2, 10, 10, texture);
        context.drawTexture(width / 2 - hudTexture.width() / 2, height - hudTexture.height(), hudTexture.width(), hudTexture.height(), hudTexture);
        context.drawText(String.valueOf(CraftEngineClient.INSTANCE.fpsCounter().fps()), 5, 5, 0.5f);
        context.drawText(positionToString(CraftEngineClient.INSTANCE.player().tickPosition()), 5, 10, 0.5f);
        context.drawText(CraftEngineClient.INSTANCE.player().camera().angle().toString(), 5, 15, 0.5f);
        ClientPlayerEntity player = CraftEngineClient.INSTANCE.player();
        double dx = player.tickPosition().x - player.previousTickPosition.x;
        double dz = player.tickPosition().y - player.previousTickPosition.y;
        double speed = Math.sqrt(dx * dx + dz * dz) * 20;
        String speedText = String.format("Speed: %.2f b/s", speed);
        context.drawText(speedText, 5, 20, 0.5f);
        for (SlotWidget slot : hotbarSlots) {
            slot.render(context, mouseX, mouseY, delta);
        }
    }
    
    public String positionToString(Vector3f position) {
        return this.decimalFormat.format(position.x) + ", " + this.decimalFormat.format(position.y) + ", " + this.decimalFormat.format(position.z);
    }
    
}