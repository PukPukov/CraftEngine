package ru.mrbedrockpy.craftengine.world.generator;

import ru.mrbedrockpy.craftengine.world.chunk.Chunk;

public interface ChunkGenerator {

    void generate(Chunk chunk);

}
