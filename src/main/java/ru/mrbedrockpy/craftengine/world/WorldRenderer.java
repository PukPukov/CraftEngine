package ru.mrbedrockpy.craftengine.world;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.graphics.Mesh;
import ru.mrbedrockpy.craftengine.graphics.Shader;
import ru.mrbedrockpy.craftengine.graphics.Texture;
import ru.mrbedrockpy.craftengine.graphics.TextureAtlas;
import ru.mrbedrockpy.craftengine.window.Camera;
import ru.mrbedrockpy.craftengine.world.entity.ClientPlayerEntity;
import ru.mrbedrockpy.craftengine.world.raycast.BlockRaycastResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class WorldRenderer {
    private final Camera camera;
    private Vector3i selectedBlock;
    private Shader shader;
    private Mesh mesh;
    private final TextureAtlas atlas;
    private final BufferedImage builtAtlas;
    private final Texture texture;

    public WorldRenderer(Camera camera) {
        this.camera = camera;
        shader = Shader.load("vertex.glsl", "fragment.glsl");
        this.atlas = new TextureAtlas(16, 16);
        try {
            loadTextures();
        } catch (IOException e) {
            e.printStackTrace();
        }
        builtAtlas = atlas.buildAtlas();
        texture = Texture.fromBufferedImage(builtAtlas);
    }

    private void loadTextures() throws IOException {
        atlas.addTile("dirt", ImageIO.read(new File("dirt.png")));
        atlas.addTile("stone", ImageIO.read(new File("stone.png")));
    }

    public void render(World world, ClientPlayerEntity player) {
        Chunk chunk = world.getChunkByChunkPos(0,0);
        mesh = chunk.getChunkMesh(camera, atlas);
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
        shader.unbind();
        texture.unbind();
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
    }
}