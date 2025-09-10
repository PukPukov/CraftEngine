package ru.mrbedrockpy.craftengine.registry;


import ru.mrbedrockpy.craftengine.world.block.Block;
import ru.mrbedrockpy.craftengine.world.item.Item;
import ru.mrbedrockpy.renderer.api.IBlock;

public class Registries {
    public static final Registry<IBlock> BLOCKS = new Registry<>();
    // Пока нет необходисти в передаче предмета в рендерер
    public static final Registry<Item> ITEMS = new Registry<>();

    public static void freeze() {
        BLOCKS.freeze();
        ITEMS.freeze();
    }
}
