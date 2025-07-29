package ru.mrbedrockpy.craftengine.world.generator;

import org.joml.Vector2i;
import ru.mrbedrockpy.craftengine.world.Chunk;
import ru.mrbedrockpy.craftengine.world.block.Blocks;

public class SimpleChunkGenerator implements ChunkGenerator {

    @Override
    public void generate(Vector2i chunkPos, Chunk chunk) {
        for (int x = 0; x < Chunk.WIDTH; x++) {
            for (int z = 0; z < Chunk.WIDTH; z++) {
                chunk.setBlock(x, 0, z, Blocks.DIRT);
            }
        }
    }
}
