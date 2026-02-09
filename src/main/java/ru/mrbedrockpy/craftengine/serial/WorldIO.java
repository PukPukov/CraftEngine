package ru.mrbedrockpy.craftengine.serial;

import org.joml.Vector2i;
import ru.mrbedrockpy.craftengine.world.World;
import ru.mrbedrockpy.craftengine.world.chunk.Chunk;
import ru.mrbedrockpy.craftengine.world.generator.PerlinChunkGenerator;

public final class WorldIO {

    private WorldIO() {}

    public static CompoundTag serialize(World world) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("size", world.getSize());

        CompoundTag chunksTag = new CompoundTag();
        for (Chunk[] row : world.getChunks()) {
            for (Chunk chunk : row) {
                if (chunk == null) continue;
                String key = chunk.getPosition().x + "," + chunk.getPosition().y;
                chunksTag.putCompound(key, ChunkIO.serialize(chunk));
            }
        }
        tag.putCompound("chunks", chunksTag);
        return tag;
    }

    public static World deserialize(CompoundTag tag) {
        final int size = tag.getInt("size");
        if (size <= 0) throw new IllegalArgumentException("Bad world size: " + size);

        World world = new World(size, PerlinChunkGenerator.DEFAULT);

        CompoundTag chunksTag = tag.getCompound("chunks");
        for (String key : chunksTag.getAllKeys()) {
            try {
                // парс ключа "x,y"
                String[] parts = key.split(",", 2);
                int x = Integer.parseInt(parts[0].trim());
                int z = Integer.parseInt(parts[1].trim());

                if (x < 0 || z < 0 || x >= size || z >= size) {
                    System.err.println("[WorldIO] Skip out-of-bounds chunk " + key + " for size=" + size);
                    continue;
                }

                CompoundTag ctag = chunksTag.getCompound(key);
                Chunk chunk = ChunkIO.deserialize(ctag);

                // сверка позиции: ключ → истина
                Vector2i pos = chunk.getPosition();
                if (pos.x != x || pos.y != z) {
                    // переносим блоки в новый чанк с верной позицией
                    short[][][] blocks = chunk.getBlocks();
                    chunk = new Chunk(new Vector2i(x, z), blocks);
                }

                world.setChunk(chunk);
            } catch (Exception ex) {
                System.err.println("[WorldIO] Failed to load chunk '" + key + "': " + ex);
                // не валим всю загрузку
            }
        }

        return world;
    }
}