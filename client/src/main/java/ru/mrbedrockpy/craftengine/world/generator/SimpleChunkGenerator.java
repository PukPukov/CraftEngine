package ru.mrbedrockpy.craftengine.world.generator;

import org.joml.Vector2i;
import ru.mrbedrockpy.craftengine.world.Chunk;
import ru.mrbedrockpy.craftengine.world.block.Blocks;
import ru.mrbedrockpy.renderer.api.IChunk;

public class SimpleChunkGenerator implements ChunkGenerator {
    
    @Override
    public void generate(Vector2i chunkPos, IChunk chunk) {
        for (int x = 0; x < Chunk.WIDTH; x++) {
            for (int y = 0; y < Chunk.WIDTH; y++) {
                chunk.setBlock(x, y, 0, Blocks.DIRT);
            }
        }
    }
}