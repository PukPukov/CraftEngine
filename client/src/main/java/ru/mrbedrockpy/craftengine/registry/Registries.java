package ru.mrbedrockpy.craftengine.registry;


import ru.mrbedrockpy.renderer.api.IBlock;

public class Registries {
    public static final Registry<IBlock> BLOCKS = new Registry<>();

    public static void freeze() {
        BLOCKS.freeze();
    }
}
