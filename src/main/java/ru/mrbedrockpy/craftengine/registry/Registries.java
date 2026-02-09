package ru.mrbedrockpy.craftengine.registry;


import ru.mrbedrockpy.craftengine.world.block.Block;
import ru.mrbedrockpy.craftengine.world.item.Item;

// Это реестры контента, их ненужно использовать для чего то еще
public class Registries {
    public static final Registry<Block> BLOCKS = new Registry<>();
    public static final Registry<Item> ITEMS = new Registry<>();

    public static void freeze() {
        BLOCKS.freeze();
        ITEMS.freeze();
    }
}
