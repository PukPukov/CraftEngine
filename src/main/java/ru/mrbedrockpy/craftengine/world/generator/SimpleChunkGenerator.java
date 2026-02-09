package ru.mrbedrockpy.craftengine.world.generator;

import ru.mrbedrockpy.craftengine.world.block.Blocks;
import ru.mrbedrockpy.craftengine.world.chunk.Chunk;

public class SimpleChunkGenerator implements ChunkGenerator {
    
    @Override
    public void generate(Chunk chunk) {
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int y = 0; y < Chunk.SIZE; y++) {
                chunk.setBlock(x, y, 0, Blocks.DIRT);
            }
        }
    }
}