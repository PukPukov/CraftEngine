package ru.mrbedrockpy.craftengine.core.world.block;

import ru.mrbedrockpy.craftengine.core.registry.Registries;

public class Blocks{

    public static final Block AIR = regBlock("air", new Block("air", false));
    public static final Block STONE = regBlock("stone", new Block("stone", true));
    public static final Block DIRT = regBlock("dirt", new Block("dirt", true));

    private static <T extends Block>T regBlock(String name, T block){
        return Registries.BLOCKS.register(name, block);
    }
    public static void register(){}
}
