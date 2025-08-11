package ru.mrbedrockpy.renderer.world;

import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import ru.mrbedrockpy.renderer.RenderInit;
import ru.mrbedrockpy.renderer.api.ICamera;
import ru.mrbedrockpy.renderer.api.IChunk;
import ru.mrbedrockpy.renderer.api.IEntity;
import ru.mrbedrockpy.renderer.api.IWorld;
import ru.mrbedrockpy.renderer.graphics.*;
import ru.mrbedrockpy.renderer.util.FileLoader;
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
        this.atlas = new TextureAtlas(16);
        try {
            loadTextures();
        } catch (IOException e) {
            e.printStackTrace();
        }
        builtAtlas = atlas.buildAtlas();
        texture = Texture.fromBufferedImage(builtAtlas);
    }
    
    private void loadTextures() throws IOException {
        atlas.addTile("dirt", FileLoader.loadImage("dirt.png"));
        atlas.addTile("stone", FileLoader.loadImage("stone.png"));
    }

    private final FrustumCuller culler = new FrustumCuller();
    
    public void render(IWorld world, IEntity player) {
        Matrix4f projView = new Matrix4f(camera.getProjectionMatrix())
            .mul(camera.getViewMatrix());
        culler.update(projView);
        shader.use();
        shader.setUniformMatrix4f("model", new Matrix4f());
        shader.setUniformMatrix4f("view", camera.getViewMatrix());
        shader.setUniformMatrix4f("projection", camera.getProjectionMatrix());
        texture.use();
        for (IChunk chunk : chunksAround(player.getChunkPosition(), RenderInit.CONFIG.getInt("render.distance"), world)) {
            if (chunk == null) continue;
            if (distanceByAxis(player.getChunkPosition(), chunk.getPosition()) > RenderInit.CONFIG.getInt("render.distance") || !culler.isBoxVisible(
                chunk.getWorldPosition().x, chunk.getWorldPosition().y, 0,
                chunk.getWorldPosition().x + IChunk.WIDTH,
                chunk.getWorldPosition().y + IChunk.WIDTH,
                IChunk.HEIGHT
            )) {
                continue;
            }
            Mesh mesh = chunk.chunkMesh(world, atlas);
            mesh.render();
//                mesh.cleanup();
        }
        shader.unbind();
        texture.unbind();
    }
    
    public void updateSelectedBlock(IWorld world, IEntity player) {
        Vector3f origin = new Vector3f(camera.getPosition());
        Vector3f direction = camera.getFront();
        
        BlockRaycastResult blockRaycastResult = world.raycast(origin, direction, 4.5f);
        if (blockRaycastResult != null) {
            selectedBlock = new Vector3i(blockRaycastResult.x, blockRaycastResult.y, blockRaycastResult.z);
        } else {
            selectedBlock = null;
        }
    }
    
    public void cleanup() {
    }

    private int distanceByAxis(Vector2i pos1, Vector2i pos2) {
        return Math.max(Math.abs(pos1.x - pos2.x), Math.abs(pos1.y - pos2.y));
    }

    private IChunk[] chunksAround(Vector2i pos, int radius, IWorld world) {
        int diameter = 2 * radius + 1;
        IChunk[] chunks = new IChunk[diameter * diameter];
        int index = 0;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                chunks[index++] = world.chunk(pos.x + dx, pos.y + dz);
            }
        }

        return chunks;
    }
}