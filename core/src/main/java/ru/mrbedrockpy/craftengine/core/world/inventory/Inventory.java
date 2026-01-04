package ru.mrbedrockpy.craftengine.core.world.inventory;


import ru.mrbedrockpy.craftengine.core.world.item.Item;
import ru.mrbedrockpy.craftengine.core.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Универсальный контейнер слотов.
 */
public class Inventory {
    protected final List<ItemStack> slots;

    public Inventory(int size) {
        slots = new ArrayList<>(Collections.nCopies(size, null));
    }

    public int size() {
        return slots.size();
    }

    public ItemStack stack(int slot) {
        return slots.get(slot);
    }

    public void stack(int slot, ItemStack stack) {
        slots.set(slot, stack);
    }

    /**
     * Пытаемся положить ItemStack в любой совместимый слот.
     * @return true, если положили полностью; false — если часть осталась.
     */
    public boolean item(ItemStack stack) {
        for (int i = 0; i < size(); i++) {
            ItemStack existing = stack(i);
            if (existing != null && existing.item().equals(stack.item())) {
                if (existing.merge(stack)) return true;
            }
        }
        int empty = slots.indexOf(null);
        if (empty >= 0) {
            stack(empty, stack);
            return true;
        }
        return false;
    }

    /**
     * Удаляет указанное количество из слота и возвращает убранную часть.
     */
    public ItemStack removeItem(int slot, int amount) {
        ItemStack existing = stack(slot);
        if (existing == null) return null;
        int removed = Math.min(amount, existing.count());
        ItemStack result = new ItemStack(existing.item(), removed);
        existing.setCount(existing.count() - removed);
        if (existing.isEmpty()) stack(slot, null);
        return result;
    }

    /**
     * Находит первый слот с данным Item, или -1.
     */
    public int setStack(Item item) {
        for (int i = 0; i < size(); i++) {
            ItemStack s = stack(i);
            if (s != null && s.item().equals(item)) {
                return i;
            }
        }
        return -1;
    }

    public void setStack(int slot, ItemStack stack){
        slots.set(slot, stack);
    }
}