package ru.mrbedrockpy.craftengine.gui.screen;

import ru.mrbedrockpy.craftengine.CraftEngineClient;
import ru.mrbedrockpy.craftengine.gui.screen.widget.SlotWidget;
import ru.mrbedrockpy.craftengine.world.inventory.PlayerInventory;
import ru.mrbedrockpy.craftengine.world.item.ItemStack;

import static ru.mrbedrockpy.craftengine.gui.screen.UI.*;

public class InventoryScreen {
    public static Screen create(PlayerInventory inv){
        return UI.create().grid(i -> slot(inv, i, InventoryScreen::slotClick), 3, 9, 0, 0, 18, 18).b();
    }

    private static void slotClick(SlotWidget slot){
        PlayerInventory inv = CraftEngineClient.INSTANCE.player().inventory();

        ItemStack cursor = inv.cursorStack();
        ItemStack slotStack = slot.stack();

        if (cursor.isEmpty()) {
            inv.cursorStack(slotStack);
            slot.stack(ItemStack.EMPTY);

        } else if (slotStack.isEmpty()) {
            slot.stack(cursor);
            inv.cursorStack(ItemStack.EMPTY);

        } else if (cursor.item().equals(slotStack.item())) {
            int max = cursor.item().maxStackSize();
            int space = max - slotStack.count();
            int toTransfer = Math.min(cursor.count(), space);

            slotStack.increment(toTransfer);
            cursor.decrement(toTransfer);

            if (cursor.isEmpty()) {
                inv.cursorStack(ItemStack.EMPTY);
            } else {
                inv.cursorStack(cursor);
            }

        } else {
            slot.stack(cursor);
            inv.cursorStack(slotStack);
        }
    }
}
