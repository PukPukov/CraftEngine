package ru.mrbedrockpy.craftengine.core.world.item;

import ru.mrbedrockpy.craftengine.core.registry.Registries;
import ru.mrbedrockpy.craftengine.core.world.block.Blocks;

public class Items {
    public static Item AIR = regItem("air", new Item("air", 1));
    public static Item GOLDEN_APPLE = regItem("gapple", new Item("gapple", 64));
    public static Item DIRT_BLOCK_ITEM = regItem("dirt", new BlockItem("dirt", 64, Blocks.DIRT));
    public static Item STONE_BLOCK_ITEM = regItem("stone", new BlockItem("stone", 64, Blocks.STONE));
    private static <T extends Item> T regItem(String id, T item) {
        Registries.ITEMS.register(id, item);
        return item;
    }

    public static void register() {}
}
