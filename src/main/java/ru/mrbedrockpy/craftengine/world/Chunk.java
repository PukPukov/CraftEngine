package ru.mrbedrockpy.craftengine.world;

import lombok.Getter;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.lwjgl.opengl.GL11;
import ru.mrbedrockpy.craftengine.graphics.Cuboid;
import ru.mrbedrockpy.craftengine.graphics.Mesh;
import ru.mrbedrockpy.craftengine.graphics.MeshBuilder;
import ru.mrbedrockpy.craftengine.world.block.Block;
import ru.mrbedrockpy.craftengine.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Chunk {

    public static final int WIDTH = 32;
    public static final int HEIGHT = 16;

    @Getter
    private final Vector2i position;
    private final Block[][][] blocks;
    private final List<LivingEntity> entities = new ArrayList<>();

    public Chunk(Vector2i position) {
        this.position = position;
        this.blocks = new Block[WIDTH][HEIGHT][WIDTH];
    }

    public Chunk(Vector2i position, Block[][][] blocks) {
        this.position = position;
        this.blocks = blocks;
    }

    public Block getBlock(int x, int y, int z) {
        try {
            return blocks[x][y][z];
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    public Block getBlock(Vector3i pos) {
        return getBlock(pos.x, pos.y, pos.z);
    }

    public boolean setBlock(int x, int y, int z, Block block) {
        try {
           blocks[x][y][z] = block;
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

    public Mesh getChunkMesh() {
        MeshBuilder builder = new MeshBuilder();

        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                for (int z = 0; z < WIDTH; z++) {
                    Block block = getBlock(x, y, z);
                    if (block == null || !block.isSolid()) continue;
                    for (Block.Direction dir : Arrays.stream(Block.Direction.values()).filter(dir -> dir != Block.Direction.NONE).toList()) {
                        Block neighbor = getBlock(dir.offset(x, y, z));
                        if (neighbor == null || !neighbor.isSolid()) {
                            builder.addFace(x, y, z, dir, block);
                        }
                    }
                }
            }
        }

        Mesh.MeshData data = builder.buildData();
        Mesh finalMesh =  Mesh.mergeMeshes(List.of(data));
        return finalMesh;
    }

    public void setEntities(List<LivingEntity> entities) {
        this.entities.clear();
        this.entities.addAll(entities);
    }


    public static float[] toFloatArray(List<Float> list) {
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public List<LivingEntity> getEntities() {
        return new ArrayList<>(entities);
    }
}