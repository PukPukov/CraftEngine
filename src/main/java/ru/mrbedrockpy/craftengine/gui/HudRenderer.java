package ru.mrbedrockpy.craftengine.gui;

import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.CraftEngineClient;
import ru.mrbedrockpy.craftengine.gui.screen.widget.SlotWidget;
import ru.mrbedrockpy.craftengine.util.id.RL;
import ru.mrbedrockpy.craftengine.world.entity.PlayerEntity;
import ru.mrbedrockpy.craftengine.world.inventory.PlayerInventory;
import ru.mrbedrockpy.craftengine.render.gui.DrawContext;

import java.text.DecimalFormat;

public class HudRenderer {
    
    private final DecimalFormat decimalFormat = new DecimalFormat();
    
    public int width;
    public int height;
    public SlotWidget[] hotbarSlots = new SlotWidget[9];
    private boolean isRenderDebug = false;
    
    public HudRenderer(int width, int height) {
        this.decimalFormat.setMaximumFractionDigits(3);
        this.decimalFormat.setMinimumFractionDigits(3);
        this.width = width;
        this.height = height;
        PlayerInventory inventory = CraftEngineClient.INSTANCE.getPlayer().getInventory();
        for (int i = 0; i < hotbarSlots.length; i++) {
            int x = width / 2 + i * 18 - 182 / 2 + 8;
            int y = height - 29;
            final int slotIndex = i;
            SlotWidget w = new SlotWidget(
                    x, y,
                    () -> inventory.getStack(slotIndex),
                    (stack) -> inventory.setStack(slotIndex, stack),
                    s -> {}
            );
            hotbarSlots[i] = w;
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.drawTextureCentred(width / 2, height / 2, 10, 10, RL.of("gui/cursor.png"));
        PlayerEntity player = CraftEngineClient.INSTANCE.getPlayer();
        if (isRenderDebug) renderDebug(context, player);
        renderHotBar(context, player, mouseX, mouseY, delta);
    }

    public void renderHotBar(DrawContext context, PlayerEntity player, int mouseX, int mouseY, float delta) {
        context.drawTexture(width / 2 - 90, height - 36, 176, 32, 0, 144, 176, 32, RL.of("gui/inventory.png"));
        for (SlotWidget slot : hotbarSlots) slot.render(context, mouseX, mouseY, delta);
        SlotWidget w = hotbarSlots[player.getInventory().getSelectedHotbarSlot()];
        context.drawTexture(w.getX() - 2, w.getY() - 2, 22, 22, RL.of("gui/selected_slot.png"));
    }

    public void renderDebug(DrawContext context, PlayerEntity player) {
        context.drawText(String.valueOf(1/CraftEngineClient.INSTANCE.getDelta()), 5, 5);
        context.drawText(positionToString(CraftEngineClient.INSTANCE.getPlayer().getPosition()), 5, 10);
        context.drawText(CraftEngineClient.INSTANCE.getPlayer().getCamera().getAngle().toString(), 5, 15);
        double dx = player.getPosition().x - player.previousTickPosition.x;
        double dz = player.getPosition().z - player.previousTickPosition.z;
        double speed = Math.sqrt(dx * dx + dz * dz) * 20;
        String speedText = String.format("Speed: %.2f b/s ", speed);
        context.drawText(speedText, 5, 20);
    }
    
    public String positionToString(Vector3f position) {
        return this.decimalFormat.format(position.x) + ", " + this.decimalFormat.format(position.y) + ", " + this.decimalFormat.format(position.z);
    }

    public void toggleDebug() {
        isRenderDebug = !isRenderDebug;
    }
}