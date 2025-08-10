package ru.mrbedrockpy.craftengine.world.generator;

import org.joml.Vector2i;
import ru.mrbedrockpy.craftengine.world.Chunk;
import ru.mrbedrockpy.renderer.api.IChunk;

public interface ChunkGenerator {

    void generate(Vector2i chunkPos, IChunk chunk);

}
