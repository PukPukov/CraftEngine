package ru.mrbedrockpy.craftengine.client.gui;

import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.client.CraftEngineClient;
import ru.mrbedrockpy.craftengine.client.gui.screen.widget.SlotWidget;
import ru.mrbedrockpy.craftengine.core.world.entity.PlayerEntity;
import ru.mrbedrockpy.craftengine.core.world.inventory.PlayerInventory;
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
        PlayerInventory inventory = CraftEngineClient.INSTANCE.getPlayer().getInventory();
        for (int i = 0; i < hotbarSlots.length; i++) {
            int x = width / 2 + i * 20 - 182 / 2 + 2;
            int y = height - 22 + 2;
            final int slotIndex = i;
            SlotWidget w = new SlotWidget(
                    x, y,
                    () -> inventory.getStack(slotIndex),
                    (stack) -> inventory.setStack(slotIndex, stack),
                    this::slotClick
            );
            hotbarSlots[i] = w;
        }
    }
    public void slotClick(SlotWidget slot) {}

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTextureCentred(width / 2, height / 2, 10, 10, "cursor.png");
        context.drawTexture(width / 2 - 182 / 2 - 1, height - 22, 184, 22, "hotbar.png");
        context.drawText(String.valueOf(CraftEngineClient.INSTANCE.getFpsCounter().getFps()), 5, 5, 0.5f);
        context.drawText(positionToString(CraftEngineClient.INSTANCE.getPlayer().getPosition()), 5, 10, 0.5f);
        context.drawText(CraftEngineClient.INSTANCE.getPlayer().getCamera().getAngle().toString(), 5, 15, 0.5f);
        PlayerEntity player = CraftEngineClient.INSTANCE.getPlayer();
        double dx = player.getPosition().x - player.previousTickPosition.x;
        double dz = player.getPosition().y - player.previousTickPosition.y;
        double speed = Math.sqrt(dx * dx + dz * dz) * 20;
        String speedText = String.format("Speed: %.2f b/s", speed);
        context.drawText(speedText, 5, 20, 0.5f);
        for (SlotWidget slot : hotbarSlots) {
            slot.render(context, mouseX, mouseY, delta);
        }
        SlotWidget w = hotbarSlots[player.getInventory().selectedHotbarSlot()];
        context.drawTexture(w.getX() - 3, w.getY() - 2, 22, 22, "selected_slot.png");
    }
    
    public String positionToString(Vector3f position) {
        return this.decimalFormat.format(position.x) + ", " + this.decimalFormat.format(position.y) + ", " + this.decimalFormat.format(position.z);
    }
}