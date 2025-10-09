package ru.mrbedrockpy.craftengine.core.world.generator;

import ru.mrbedrockpy.craftengine.core.world.chunk.Chunk;

public interface ChunkGenerator {

    void generate(Chunk chunk);

}
