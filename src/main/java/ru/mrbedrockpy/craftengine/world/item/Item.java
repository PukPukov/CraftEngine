package ru.mrbedrockpy.craftengine.world.item;

import lombok.Getter;
import ru.mrbedrockpy.craftengine.world.entity.PlayerEntity;

public class Item{
    @Getter
    private final String displayName;
    @Getter
    private final int maxStackSize;

    public Item(String displayName, int maxStackSize) {
        this.displayName = displayName;
        this.maxStackSize = maxStackSize;
    }

    public void use(PlayerEntity player) {}

    public ItemStack getDefStack(){
        return new ItemStack(this);
    }
}