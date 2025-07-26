package ru.mrbedrockpy.craftengine.world;

import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.CraftEngineClient;
import ru.mrbedrockpy.craftengine.graphics.Cuboid;
import ru.mrbedrockpy.craftengine.graphics.Mesh;
import ru.mrbedrockpy.craftengine.graphics.Shader;
import ru.mrbedrockpy.craftengine.graphics.Texture;
import ru.mrbedrockpy.craftengine.window.Camera;
import ru.mrbedrockpy.craftengine.world.block.Block;
import ru.mrbedrockpy.craftengine.world.entity.ClientPlayerEntity;
import ru.mrbedrockpy.craftengine.world.raycast.BlockRaycastResult;

import java.util.ArrayList;
import java.util.List;

public class WorldRenderer {

    private final Cuboid[][][] cuboids;
    private final Camera camera;
    private final int width, height, depth;
    private Vector3i selectedBlock;
    private Shader shader;
    private Texture texture;
    private Mesh mesh;

    public WorldRenderer(Camera camera, int width, int height, int depth) {
        this.camera = camera;
        this.width = width;
        this.height = height;
        this.depth = depth;

        texture = Texture.load("block.png");
        shader = Shader.load("vertex.glsl", "fragment.glsl");
        cuboids = new Cuboid[width][height][depth];
    }

    public void addCuboid(int x, int y, int z, Cuboid cuboid) {
        if (inBounds(x, y, z)) {
            if (cuboids[x][y][z] != null) {
                cuboids[x][y][z].cleanup();
            }
            cuboids[x][y][z] = cuboid;
        }
    }

    public void removeCuboid(int x, int y, int z) {
        if (inBounds(x, y, z) && cuboids[x][y][z] != null) {
            cuboids[x][y][z].cleanup();
            cuboids[x][y][z] = null;
        }
    }

    private boolean inBounds(int x, int y, int z) {
        return x >= 0 && x < width
            && y >= 0 && y < height
            && z >= 0 && z < depth;
    }

    public void render(World world, ClientPlayerEntity player) {
        Chunk chunk = world.getChunkByChunkPos(0,0);
        mesh = chunk.getChunkMesh();
        shader.use();
        shader.setUniformMatrix4f("model", new Matrix4f());
        shader.setUniformMatrix4f("view", camera.getViewMatrix());
        shader.setUniformMatrix4f("projection", camera.getProjectionMatrix());
        texture.use();
//        for(Chunk[] chunks : world.getChunks()){
//            for (Chunk chunk : chunks){
//                if(chunk == null || chunk.getChunkMesh() == null) continue;
//            }
//        }
        mesh.render();
        texture.unbind();
        shader.unbind();
        mesh.cleanup();
    }

    public void updateSelectedBlock(World world, ClientPlayerEntity player) {
        Vector3f origin = new Vector3f(camera.getPosition()).add(0, player.getEyeOffset(), 0);
        Vector3f direction = camera.getFront();

        BlockRaycastResult blockRaycastResult = world.raycast(origin, direction, 4.5f);
        if(blockRaycastResult != null) {
            selectedBlock = new Vector3i(blockRaycastResult.x, blockRaycastResult.y, blockRaycastResult.z);
        } else {
            selectedBlock = null;
        }
    }


    public void cleanup() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    if (cuboids[x][y][z] != null) {
                        cuboids[x][y][z].cleanup();
                    }
                }
            }
        }
    }
}