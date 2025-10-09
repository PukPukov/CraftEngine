package ru.mrbedrockpy.craftengine.core.world.chunk;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.joml.Vector2i;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.core.phys.AABB;
import ru.mrbedrockpy.craftengine.core.registry.Registries;
import ru.mrbedrockpy.craftengine.core.world.block.Block;
import ru.mrbedrockpy.craftengine.core.world.block.Blocks;
import ru.mrbedrockpy.craftengine.core.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(of = "position")
public class Chunk {
    
    @Getter
    private final Vector2i position;
    @Getter
    private final short[][][] blocks;
    @Getter
    private boolean dirty = true;
    private final List<Entity> entities = new ArrayList<>();
    public static final int SIZE = 16;
    
    public Chunk(Vector2i position) {
        this.position = position;
        this.blocks = new short[SIZE][SIZE][SIZE];
    }
    
    public Chunk(Vector2i position, short[][][] blocks) {
        this.position = position;
        this.blocks = blocks;
    }
    
    public Block getBlock(int x, int y, int z) {
        try {
            return Registries.BLOCKS.get(blocks[x][y][z]);
        } catch (IndexOutOfBoundsException e) {
            return Blocks.AIR;
        }
    }
    
    public Block getBlock(Vector3i pos) {
        return getBlock(pos.x, pos.y, pos.z);
    }
    
    public boolean setBlock(int x, int y, int z, Block block) {
        try {
            short newId = (short) Registries.BLOCKS.getId(block);
            if (blocks[x][y][z] != newId) {
                blocks[x][y][z] = newId;
                markDirty();
            }
            return true;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }
    
    public void tick() {
    }

    public void markDirty() {
        dirty = true;
    }
    
    public void setEntities(List<Entity> entities) {
        this.entities.clear();
        this.entities.addAll(entities);
    }
    
    public List<Entity> getEntities() {
        return new ArrayList<>(entities);
    }
    
    public Vector2i getWorldPosition() {
        return new Vector2i(position.x * 16, position.y * 16);
    }

    @Override
    public String toString() {
        return position.x + "," + position.y;
    }

    public AABB getAABB() {
        Vector3i min = new Vector3i(position.x * SIZE, position.y * SIZE, 0);
        Vector3i max = new Vector3i(min.x + SIZE, min.y + SIZE, min.z + SIZE);
        return new AABB(min.x, min.y, min.z, max.x, max.y, max.z);
    }
}