package ru.mrbedrockpy.craftengine.world;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.graphics.FrustumCuller;
import ru.mrbedrockpy.craftengine.graphics.Mesh;
import ru.mrbedrockpy.craftengine.graphics.MeshBuilder;
import ru.mrbedrockpy.craftengine.graphics.TextureAtlas;
import ru.mrbedrockpy.craftengine.registry.Registries;
import ru.mrbedrockpy.craftengine.window.Camera;
import ru.mrbedrockpy.craftengine.world.block.Block;
import ru.mrbedrockpy.craftengine.world.block.Blocks;
import ru.mrbedrockpy.craftengine.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Chunk {
    
    public static final int WIDTH = 32;
    public static final int HEIGHT = 16;
    
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
    
    public Block getBlock(int x, int y, int z) {
        try {
            return Registries.BLOCKS.getById(blocks[x][y][z]);
        } catch (IndexOutOfBoundsException e) {
            return null;
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
        for (LivingEntity entity : entities) {
            entity.tick();
        }
    }
    
    public Mesh getChunkMesh(World world, TextureAtlas atlas) {
        if (mesh == null || dirty) {
            if (mesh != null) {
                mesh.cleanup();
            }
            
            MeshBuilder builder = new MeshBuilder(atlas);
            for (int x = 0; x < WIDTH; x++) {
                for (int y = 0; y < WIDTH; y++) {
                    for (int z = 0; z < HEIGHT; z++) {
                        Block block = getBlock(x, y, z);
                        if (block == null || block == Blocks.AIR || !block.isSolid()) {
                            continue;
                        }
                        
                        int worldX = position.x * WIDTH + x;
                        int worldY = position.y * WIDTH + y;
                        
                        for (Block.Direction dir : Block.Direction.values()) {
                            if (dir == Block.Direction.NONE) continue;
                            
                            int neighborX = worldX + dir.dx;
                            int neighborY = worldY + dir.dy;
                            int neighborZ = z + dir.dz;
                            
                            Block neighborBlock = world.getBlock(neighborX, neighborY, neighborZ);
                            
                            // If neighbor is null (outside world) or not solid (like air), draw the face
                            if (neighborBlock == null || !neighborBlock.isSolid()) {
                                builder.addFace(worldX, worldY, z, dir, block);
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
}