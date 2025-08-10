package ru.mrbedrockpy.craftengine.gui.screen;

import ru.mrbedrockpy.craftengine.CraftEngineClient;
import ru.mrbedrockpy.craftengine.gui.screen.widget.SlotWidget;
import ru.mrbedrockpy.craftengine.registry.Registries;
import ru.mrbedrockpy.craftengine.world.inventory.PlayerInventory;
import ru.mrbedrockpy.craftengine.world.item.ItemStack;
import ru.mrbedrockpy.renderer.graphics.Texture;
import ru.mrbedrockpy.renderer.gui.DrawContext;

import static ru.mrbedrockpy.craftengine.gui.screen.UI.*;

public class InventoryScreen {
    
    private static PlayerInventory inventory;
    
    public static Screen create(PlayerInventory inv) {
        inventory = inv;
        return UI.create().grid(i -> slot(inv, i, InventoryScreen::slotClick), 3, 9, 0, 0, 0, 0)
            .render(InventoryScreen::render).b();
    }
    
    public static void slotClick(SlotWidget slot) {
        ItemStack cursor = inventory.getCursorStack();
        ItemStack slotStack = slot.stack();
        
        if (cursor.isEmpty()) {
            inventory.setCursorStack(slotStack);
            slot.stack(ItemStack.EMPTY);
        } else if (slotStack.isEmpty()) {
            slot.stack(cursor);
            inventory.setCursorStack(ItemStack.EMPTY);
            
        } else if (cursor.item().equals(slotStack.item())) {
            int max = cursor.item().maxStackSize();
            int space = max - slotStack.count();
            int toTransfer = Math.min(cursor.count(), space);
            
            slotStack.increment(toTransfer);
            cursor.decrement(toTransfer);
            
            if (cursor.isEmpty()) {
                inventory.setCursorStack(ItemStack.EMPTY);
            } else {
                inventory.setCursorStack(cursor);
            }
            
        } else {
            slot.stack(cursor);
            inventory.setCursorStack(slotStack);
        }
    }
    
    private static void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ItemStack cursorStack = inventory.getCursorStack();
        if (!cursorStack.isEmpty()) {
            context.drawTexture(mouseX - 8, mouseY - 8, 16, 16, Registries.ITEMS.getName(cursorStack.item()) + ".png");
        }
    }
    
}