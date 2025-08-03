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
        ItemStack cursor = inventory.cursorStack();
        ItemStack slotStack = slot.stack();
        
        if (cursor.isEmpty()) {
            inventory.cursorStack(slotStack);
            slot.stack(ItemStack.EMPTY);
        } else if (slotStack.isEmpty()) {
            slot.stack(cursor);
            inventory.cursorStack(ItemStack.EMPTY);
            
        } else if (cursor.item().equals(slotStack.item())) {
            int max = cursor.item().maxStackSize();
            int space = max - slotStack.count();
            int toTransfer = Math.min(cursor.count(), space);
            
            slotStack.increment(toTransfer);
            cursor.decrement(toTransfer);
            
            if (cursor.isEmpty()) {
                inventory.cursorStack(ItemStack.EMPTY);
            } else {
                inventory.cursorStack(cursor);
            }
            
        } else {
            slot.stack(cursor);
            inventory.cursorStack(slotStack);
        }
    }
    
    private static void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ItemStack cursorStack = inventory.cursorStack();
        if (!cursorStack.isEmpty()) {
            context.drawTexture(mouseX - 8, mouseY - 8, 16, 16, Texture.load(Registries.ITEMS.name(cursorStack.item()) + ".png"));
        }
    }
    
}