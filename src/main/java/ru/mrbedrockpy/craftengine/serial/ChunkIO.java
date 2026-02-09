package ru.mrbedrockpy.craftengine.serial;

import org.joml.Vector2i;
import ru.mrbedrockpy.craftengine.registry.Registries;
import ru.mrbedrockpy.craftengine.world.block.Block;
import ru.mrbedrockpy.craftengine.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.List;

public final class ChunkIO {

    private ChunkIO() {}

    public static CompoundTag serialize(Chunk chunk) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", chunk.getPosition().x);
        tag.putInt("y", chunk.getPosition().y);

        List<Integer> flat = new ArrayList<>(Chunk.SIZE * Chunk.SIZE * Chunk.SIZE);
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int y = 0; y < Chunk.SIZE; y++) {
                for (int z = 0; z < Chunk.SIZE; z++) {
                    Block b = chunk.getBlock(x,y,z);
                    int id = Registries.BLOCKS.getId(b);
                    flat.add(id);
                }
            }
        }
        tag.putList("blocks", flat);
        return tag;
    }

    public static Chunk deserialize(CompoundTag tag) {
        final int cx = tag.getInt("x");
        final int cz = tag.getInt("y");

        final List<Object> list = tag.getList("blocks");
        final int expected = Chunk.SIZE * Chunk.SIZE * Chunk.SIZE;
        if (list.size() != expected) {
            throw new IllegalArgumentException("Chunk[" + cx + "," + cz + "] bad 'blocks' length=" + list.size() + ", expected=" + expected);
        }

        short[][][] blocks = new short[Chunk.SIZE][Chunk.SIZE][Chunk.SIZE];
        int idx = 0;
        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int y = 0; y < Chunk.SIZE; y++) {
                for (int z = 0; z < Chunk.SIZE; z++) {
                    Object v = list.get(idx++);
                    if (!(v instanceof Number n)) {
                        throw new IllegalArgumentException("Chunk[" + cx + "," + cz + "] blocks[" + (idx-1) + "] not a number: " + v);
                    }
                    int id = n.intValue();
                    if (id < Short.MIN_VALUE || id > Short.MAX_VALUE) {
                        throw new IllegalArgumentException("Chunk[" + cx + "," + cz + "] id out of short range: " + id);
                    }
                    blocks[x][y][z] = (short) id;
                }
            }
        }

        return new Chunk(new Vector2i(cx, cz), blocks);
    }
}