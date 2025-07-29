package ru.mrbedrockpy.craftengine.world;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3i;
import ru.mrbedrockpy.renderer.api.IBlock;
import ru.mrbedrockpy.renderer.api.ICamera;
import ru.mrbedrockpy.renderer.graphics.Mesh;
import ru.mrbedrockpy.renderer.graphics.MeshBuilder;
import ru.mrbedrockpy.renderer.graphics.TextureAtlas;
import ru.mrbedrockpy.craftengine.registry.Registries;
import ru.mrbedrockpy.craftengine.window.Camera;
import ru.mrbedrockpy.craftengine.world.block.Block;
import ru.mrbedrockpy.craftengine.world.block.Blocks;
import ru.mrbedrockpy.craftengine.world.entity.LivingEntity;
import ru.mrbedrockpy.renderer.api.IChunk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Chunk implements IChunk {
    @Getter
    private final Vector2i position;
    private final short[][][] blocks;
    private Mesh mesh;
    @Getter
    private boolean dirty = true;
    private final List<LivingEntity> entities = new ArrayList<>();
    
    public Chunk(Vector2i position) {
        this.position = position;
        this.blocks = new short[WIDTH][WIDTH][HEIGHT];
    }
    
    public Chunk(Vector2i position, short[][][] blocks) {
        this.position = position;
        this.blocks = blocks;
        this.dirty = true;
    }
    
    public IBlock getBlock(int x, int y, int z) {
        try {
            return Registries.BLOCKS.getById(blocks[x][y][z]);
        } catch (IndexOutOfBoundsException e) {
            return null;
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
        for (LivingEntity entity : entities) {
            entity.tick();
        }
    }
    
    public Mesh getChunkMesh(ICamera camera, TextureAtlas atlas) {
        if (mesh == null || dirty) {
            if (mesh != null) {
                mesh.cleanup();
            }
            
            MeshBuilder builder = new MeshBuilder(atlas);
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < WIDTH; y++) {
                    for (int z = 0; z < HEIGHT; z++) {
                        IBlock block = getBlock(x, y, z);
                        if (block == Blocks.AIR || !block.isSolid()) continue;
                        List<Block.Direction> dirs = new ArrayList<>();
                        for(Block.Direction dir : Arrays.stream(Block.Direction.values()).filter(dir -> dir != IBlock.Direction.NONE).toList()) {
                            IBlock neighbor = getBlock(new Vector3i(x, y, z).add(dir.offset()));
                            if (neighbor != Blocks.AIR || !neighbor.isSolid()) {
                                dirs.add(dir);
                            }
                        }
                        float worldX = position.x * WIDTH + x;
                        float worldY = position.y * WIDTH + y;
                        float worldZ = z;
                        builder.addFaces((int) worldX, (int) worldY, (int) worldZ, dirs, block);
                    }
                }
            }
            Mesh.MeshData data = builder.buildData();
            mesh = Mesh.mergeMeshes(List.of(data));
            dirty = false;
        }
        return mesh;
    }
    
    public void markDirty() {
        dirty = true;
    }
    
    public void setEntities(List<LivingEntity> entities) {
        this.entities.clear();
        this.entities.addAll(entities);
    }
    
    public List<LivingEntity> getEntities() {
        return new ArrayList<>(entities);
    }
    
    public Vector2i getWorldPosition() {
        return new Vector2i(position.x * WIDTH, position.y * WIDTH);
    }

    @Override
    public void cleanup() {
        if(mesh != null) {
            mesh.cleanup();
            mesh = null;
        }
    }
}