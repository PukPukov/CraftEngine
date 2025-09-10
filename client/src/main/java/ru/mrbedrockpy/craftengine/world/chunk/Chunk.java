package ru.mrbedrockpy.craftengine.world.chunk;

import lombok.Getter;
import org.joml.Vector2i;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.registry.Registries;
import ru.mrbedrockpy.craftengine.world.block.Blocks;
import ru.mrbedrockpy.craftengine.world.entity.Entity;
import ru.mrbedrockpy.renderer.api.IBlock;
import ru.mrbedrockpy.renderer.graphics.Mesh;

import java.util.ArrayList;
import java.util.List;

public class Chunk {
    
    @Getter
    private final Vector2i position;
    @Getter
    private final short[][][] blocks;
    private Mesh mesh;
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
    
    public IBlock getBlock(int x, int y, int z) {
        try {
            return Registries.BLOCKS.get(blocks[x][y][z]);
        } catch (IndexOutOfBoundsException e) {
            return Blocks.AIR;
        }
    }
    
    public IBlock getBlock(Vector3i pos) {
        return getBlock(pos.x, pos.y, pos.z);
    }
    
    public boolean setBlock(int x, int y, int z, IBlock block) {
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
    
    public List<Entity> entities() {
        return new ArrayList<>(entities);
    }
    
    public Vector2i getWorldPosition() {
        return new Vector2i(position.x * 16, position.y * 16);
    }

    public void cleanup() {
        if(mesh != null){
            mesh.cleanup();
            mesh = null;
        }
    }
}