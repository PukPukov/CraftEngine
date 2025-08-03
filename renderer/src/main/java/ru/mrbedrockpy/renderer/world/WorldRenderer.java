package ru.mrbedrockpy.renderer.world;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import ru.mrbedrockpy.renderer.api.ICamera;
import ru.mrbedrockpy.renderer.api.IChunk;
import ru.mrbedrockpy.renderer.api.IEntity;
import ru.mrbedrockpy.renderer.api.IWorld;
import ru.mrbedrockpy.renderer.graphics.*;
import ru.mrbedrockpy.renderer.world.raycast.BlockRaycastResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class WorldRenderer {
    private final ICamera camera;
    private Vector3i selectedBlock;
    private Shader shader;
    private final TextureAtlas atlas;
    private final BufferedImage builtAtlas;
    private final Texture texture;
    
    public WorldRenderer(ICamera camera) {
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
        atlas.addTile("dirt", ImageIO.read(getClass().getClassLoader().getResourceAsStream("dirt.png")));
        atlas.addTile("stone", ImageIO.read(getClass().getClassLoader().getResourceAsStream("stone.png")));
    }
    private final FrustumCuller culler = new FrustumCuller();
    
    public void render(IWorld world, IEntity player) {
        Matrix4f projView = new Matrix4f(camera.projectionMatrix())
            .mul(camera.viewMatrix());
        culler.update(projView);
        shader.use();
        shader.setUniformMatrix4f("model", new Matrix4f());
        shader.setUniformMatrix4f("view", camera.viewMatrix());
        shader.setUniformMatrix4f("projection", camera.projectionMatrix());
        texture.use();
        for(IChunk[] chunks : world.chunks()){
            for (IChunk chunk : chunks){
                if (chunk == null) continue;
                if (player.chunkPosition().gridDistance(chunk.position()) > 8 || !culler.isBoxVisible(
                        chunk.worldPosition().x, chunk.worldPosition().y, 0,
                        chunk.worldPosition().x + IChunk.WIDTH,
                        chunk.worldPosition().y + IChunk.WIDTH,
                        IChunk.HEIGHT
                )) {
                    continue;
                }
                Mesh mesh = chunk.chunkMesh(world, atlas);
                mesh.render();
//                mesh.cleanup();
            }
        }
        shader.unbind();
        texture.unbind();
    }
    
    public void updateSelectedBlock(IWorld world, IEntity player) {
        Vector3f origin = new Vector3f(camera.position());
        Vector3f direction = camera.front();
        
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