package ru.mrbedrockpy.craftengine.core.world.block;

import ru.mrbedrockpy.craftengine.core.registry.Registries;
import ru.mrbedrockpy.craftengine.core.util.id.RL;

public class Blocks{

    public static final Block AIR = regBlock("air", new Block("air", false));
    public static final Block STONE = regBlock("stone", new Block("stone", true));
    public static final Block DIRT = regBlock("dirt", new Block("dirt", true));

    private static <T extends Block>T regBlock(String name, T block){
        return Registries.BLOCKS.register(RL.of(name), block);
    }
    public static void register(){}
}
