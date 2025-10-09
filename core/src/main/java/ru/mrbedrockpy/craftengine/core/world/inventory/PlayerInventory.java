package ru.mrbedrockpy.craftengine.core.world.inventory;

import lombok.Getter;
import lombok.Setter;
import ru.mrbedrockpy.craftengine.core.world.item.ItemStack;

/**
 * Инвентарь игрока:
 * — отдельный хотбар;
 * — ячейки брони;
 * — указатель выбранного слота в хотбаре.
 */
public class PlayerInventory extends Inventory {
    public static final int HOTBAR_SIZE = 9;
    private final ItemStack[] armorSlots = new ItemStack[4];
    private int selectedHotbarSlot = 0;
    @Getter @Setter
    private ItemStack cursorStack = ItemStack.EMPTY;

    public PlayerInventory() {
        super(36);
    }

    public int selectedHotbarSlot() {
        return selectedHotbarSlot;
    }

    public void selectedHotbarSlot(int index) {
        if (index < 0 || index >= HOTBAR_SIZE) throw new IndexOutOfBoundsException();
        selectedHotbarSlot = index;
    }

    public void setSelectedSlotStack(ItemStack stack){
        slots.set(selectedHotbarSlot, stack);
    }

    /** Текущий «активный» предмет в руке */
    public ItemStack getSelectedStack() {
        return slots.get(selectedHotbarSlot);
    }

    public void armor(int slotIndex, ItemStack armorStack) {
        if (slotIndex < 0 || slotIndex >= armorSlots.length) {
            throw new IndexOutOfBoundsException();
        }
        armorSlots[slotIndex] = armorStack;
    }

    public ItemStack armor(int slotIndex) {
        return armorSlots[slotIndex];
    }

    public void setStack(int i, ItemStack stack){
        slots.set(i, stack);
    }

    public ItemStack getStack(int i){
        return slots.get(i);
    }
}