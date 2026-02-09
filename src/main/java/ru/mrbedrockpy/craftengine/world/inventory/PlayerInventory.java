package ru.mrbedrockpy.craftengine.world.inventory;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Range;
import ru.mrbedrockpy.craftengine.world.item.ItemStack;

/**
 * Инвентарь игрока:
 * — отдельный хотбар;
 * — ячейки брони;
 * — указатель выбранного слота в хотбаре.
 */
@Getter
public class PlayerInventory extends Inventory {
    public static final int HOTBAR_SIZE = 9;
    private final ItemStack[] armorSlots = new ItemStack[4];
    @Setter
    private int selectedHotbarSlot = 0;
    @Setter
    private ItemStack cursorStack = ItemStack.EMPTY;

    public PlayerInventory() {
        super(37);
    }

    public void setSelectedSlotStack(ItemStack stack){
        slots.set(selectedHotbarSlot, stack);
    }

    public ItemStack getSelectedStack() {
        return slots.get(selectedHotbarSlot);
    }

    public void setArmor(@Range(from = 0, to = 3) int slotIndex, ItemStack armorStack) {
        armorSlots[slotIndex] = armorStack;
    }

    public ItemStack getArmor(@Range(from = 0, to = 3)int slotIndex) {
        return armorSlots[slotIndex];
    }

    public void setStack(int i, ItemStack stack){
        slots.set(i, stack);
    }

    public ItemStack getStack(int i){
        return slots.get(i);
    }
}