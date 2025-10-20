package ru.mrbedrockpy.craftengine.core.registry;


import ru.mrbedrockpy.craftengine.core.world.block.Block;
import ru.mrbedrockpy.craftengine.core.world.item.Item;

// Это реестры контента, их ненужно использовать для чего то еще
public class Registries {
    public static final Registry<Block> BLOCKS = new Registry<>();
    public static final Registry<Item> ITEMS = new Registry<>();

    public static void freeze() {
        BLOCKS.freeze();
        ITEMS.freeze();
    }
}
