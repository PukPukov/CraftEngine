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
            hotbarSlots[i] = UI.slot(CraftEngineClient.INSTANCE.getPlayer().getInventory(), i, InventoryScreen::slotClick);
            hotbarSlots[i].setPosition(width / 2 + i * 20 - 182 / 2 + 2, height - 22 + 2);
        }
    }
    

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTextureCentred(width / 2, height / 2, 10, 10, "cursor.png");
        context.drawTexture(width / 2 - 182 / 2, height - 22, 182, 22, "hotbar.png");
        context.drawText(String.valueOf(CraftEngineClient.INSTANCE.getFpsCounter().getFps()), 5, 5, 0.5f);
        context.drawText(positionToString(CraftEngineClient.INSTANCE.getPlayer().getTickPosition()), 5, 10, 0.5f);
        context.drawText(CraftEngineClient.INSTANCE.getPlayer().getCamera().getAngle().toString(), 5, 15, 0.5f);
        ClientPlayerEntity player = CraftEngineClient.INSTANCE.getPlayer();
        double dx = player.getTickPosition().x - player.previousTickPosition.x;
        double dz = player.getTickPosition().y - player.previousTickPosition.y;
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