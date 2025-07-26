package ru.mrbedrockpy.craftengine.registry;

import ru.mrbedrockpy.craftengine.world.block.Block;

public class Registries {
    public static final Registry<Block> BLOCKS = new Registry<>();

    public static void freeze() {
        BLOCKS.freeze();
    }
}
