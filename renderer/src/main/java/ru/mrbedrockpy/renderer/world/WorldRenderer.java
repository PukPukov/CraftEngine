package ru.mrbedrockpy.craftengine.world;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.graphics.*;
import ru.mrbedrockpy.craftengine.window.Camera;
import ru.mrbedrockpy.craftengine.world.entity.ClientPlayerEntity;
import ru.mrbedrockpy.craftengine.world.raycast.BlockRaycastResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class WorldRenderer {
    private final Camera camera;
    private Vector3i selectedBlock;
    private Shader shader;
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
    private final FrustumCuller culler = new FrustumCuller();
    
    public void render(World world, ClientPlayerEntity player) {
        Matrix4f projView = new Matrix4f(camera.getProjectionMatrix())
            .mul(camera.getViewMatrix());
        culler.update(projView);
        shader.use();
        shader.setUniformMatrix4f("model", new Matrix4f());
        shader.setUniformMatrix4f("view", camera.getViewMatrix());
        shader.setUniformMatrix4f("projection", camera.getProjectionMatrix());
        texture.use();
        for(Chunk[] chunks : world.getChunks()){
            for (Chunk chunk : chunks){
                if (chunk == null) continue;
                if (!culler.isBoxVisible(
                    chunk.getWorldPosition().x, chunk.getWorldPosition().y, 0,
                    chunk.getWorldPosition().x + Chunk.WIDTH,
                    chunk.getWorldPosition().y + Chunk.WIDTH,
                    Chunk.HEIGHT
                )) {
                    continue;
                }
                Mesh mesh = chunk.getChunkMesh(world, atlas);
                mesh.render();
//                mesh.cleanup();
            }
        }
        shader.unbind();
        texture.unbind();
    }
    
    public void updateSelectedBlock(World world, ClientPlayerEntity player) {
        Vector3f origin = new Vector3f(camera.getPosition());
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