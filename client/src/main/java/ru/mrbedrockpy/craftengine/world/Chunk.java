package ru.mrbedrockpy.craftengine.world;

import lombok.Getter;
import org.joml.Vector2i;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.registry.Registries;
import ru.mrbedrockpy.craftengine.world.block.Blocks;
import ru.mrbedrockpy.renderer.api.IBlock;
import ru.mrbedrockpy.renderer.api.IChunk;
import ru.mrbedrockpy.renderer.api.IEntity;
import ru.mrbedrockpy.renderer.api.IWorld;
import ru.mrbedrockpy.renderer.graphics.Mesh;
import ru.mrbedrockpy.renderer.graphics.MeshBuilder;
import ru.mrbedrockpy.renderer.graphics.TextureAtlas;

import java.util.ArrayList;
import java.util.List;

public class Chunk implements IChunk {
    
    @Getter
    private final Vector2i position;
    private final short[][][] blocks;
    private Mesh mesh;
    @Getter
    private boolean dirty = true;
    private final List<IEntity> entities = new ArrayList<>();
    
    public Chunk(Vector2i position) {
        this.position = position;
        this.blocks = new short[WIDTH][WIDTH][HEIGHT];
    }
    
    public Chunk(Vector2i position, short[][][] blocks) {
        this.position = position;
        this.blocks = blocks;
    }
    
    @Override
    public IBlock block(int x, int y, int z) {
        try {
            return Registries.BLOCKS.get(blocks[x][y][z]);
        } catch (IndexOutOfBoundsException e) {
            return Blocks.AIR;
        }
    }
    
    @Override
    public IBlock block(Vector3i pos) {
        return block(pos.x, pos.y, pos.z);
    }
    
    @Override
    public boolean setBlock(int x, int y, int z, IBlock block) {
        try {
            short newId = (short) Registries.BLOCKS.id(block);
            if (blocks[x][y][z] != newId) {
                blocks[x][y][z] = newId;
                markDirty();
            }
            return true;
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }
    
    @Override
    public void tick() {
    }
    
    @Override
    public Mesh chunkMesh(IWorld world, TextureAtlas atlas) {
        if (mesh == null || dirty) {
            if (mesh != null) {
                mesh.cleanup();
            }
            
            MeshBuilder builder = new MeshBuilder(atlas);
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < WIDTH; y++) {
                    for (int z = 0; z < HEIGHT; z++) {
                        IBlock block = block(x, y, z);
                        if (block == Blocks.AIR || !block.isSolid()) {
                            continue;
                        }
                        
                        int worldX = position.x * WIDTH + x;
                        int worldY = position.y * WIDTH + y;
                        
                        for (IBlock.Direction dir : IBlock.Direction.values()) {
                            if (dir == IBlock.Direction.NONE) continue;
                            
                            int neighborX = worldX + dir.dx;
                            int neighborY = worldY + dir.dy;
                            int neighborZ = z + dir.dz;
                            
                            IBlock neighborBlock = world.block(neighborX, neighborY, neighborZ);
                            
                            if (neighborBlock == Blocks.AIR || !neighborBlock.isSolid()) {
                                builder.addFace(worldX, worldY, z, dir, block, world);
                            }
                        }
                    }
                }
            }
            Mesh.MeshData data = builder.buildData();
            mesh = Mesh.mergeMeshes(List.of(data));
            dirty = false;
        }
        return mesh;
    }
    
    @Override
    public void markDirty() {
        dirty = true;
    }
    
    @Override
    public void setEntities(List<IEntity> entities) {
        this.entities.clear();
        this.entities.addAll(entities);
    }
    
    public List<IEntity> entities() {
        return new ArrayList<>(entities);
    }
    
    @Override
    public Vector2i worldPosition() {
        return new Vector2i(position.x * WIDTH, position.y * WIDTH);
    }

    @Override
    public void cleanup() {
        if(mesh != null){
            mesh.cleanup();
            mesh = null;
        }
    }
}