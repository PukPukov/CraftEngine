package ru.mrbedrockpy.craftengine.world.item;

import ru.mrbedrockpy.craftengine.world.entity.ClientPlayerEntity;

public class Item {
    private final String displayName;
    private final int maxStackSize;

    /**
     * Конструктор для создания предмета.
     * в будущем могут быть приватные поля, поэтому без lombok.
     * @param displayName Отображаемое имя предмета
     * @param maxStackSize Максимальный размер стека предметов
     */
    public Item(String displayName, int maxStackSize) {
        this.displayName = displayName;
        this.maxStackSize = maxStackSize;
    }

    public String displayName() { return displayName; }
    public int maxStackSize() { return maxStackSize; }

    /**
     * Вызывается, когда игрок «использует» предмет.
     * Сейчас ничего не происходит, но в будущем можно будет добавить логику использования предметов.
     * ClientPlayerEntity - надо разделить на Player и ClientPlayerEntity, чтобы не было путаницы.
     * @param player Игрок, использующий предмет
     */
    public void use(ClientPlayerEntity player) {}
}