package ru.mrbedrockpy.craftengine.world.block;

import ru.mrbedrockpy.craftengine.registry.Registries;

public class Blocks {
    public static final Block AIR = registerBlock("air", new Block("air", false));
    public static final Block STONE = registerBlock("stone", new Block("stone", true));
    public static final Block DIRT = registerBlock("dirt", new Block("dirt", true));

    private static <T extends Block>T registerBlock(String name, T block) {
        return Registries.BLOCKS.register(name, block);
    }
    public static void register(){}
}