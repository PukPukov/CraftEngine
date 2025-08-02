package ru.mrbedrockpy.craftengine.world.item;

import ru.mrbedrockpy.craftengine.registry.Registries;

public class Items {
    public static Item AIR = regItem("air", new Item("air", 1));
    public static Item GOLDEN_APPLE = regItem("gapple", new Item("gapple", 1));
    private static <T extends Item> T regItem(String id, T item) {
        Registries.ITEMS.register(id, item);
        return item;
    }

    public static void register() {}
}
