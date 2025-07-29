package ru.mrbedrockpy.renderer.world;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;
import ru.mrbedrockpy.renderer.api.*;
import ru.mrbedrockpy.renderer.graphics.*;
import ru.mrbedrockpy.renderer.world.raycast.BlockRaycastResult;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
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

    // TODO: сделать систему загрузки текстур
    private void loadTextures() throws IOException {
        atlas.addTile("dirt", ImageIO.read(getClass().getClassLoader().getResourceAsStream("dirt.png")));
        atlas.addTile("stone", ImageIO.read(getClass().getClassLoader().getResourceAsStream("stone.png")));
    }
    private final FrustumCuller culler = new FrustumCuller();

    public void render(IWorld world, IPlayerEntity player) {
        Matrix4f projView = new Matrix4f(camera.getProjectionMatrix())
                .mul(camera.getViewMatrix());
        culler.update(projView);
        shader.use();
        shader.setUniformMatrix4f("model", new Matrix4f());
        shader.setUniformMatrix4f("view", camera.getViewMatrix());
        shader.setUniformMatrix4f("projection", camera.getProjectionMatrix());
        texture.use();
        for (IChunk[] chunks : world.getChunks()) {
            for (IChunk chunk : chunks) {
                if (!culler.isBoxVisible(
                        chunk.getWorldPosition().x, 0, chunk.getWorldPosition().y,
                        chunk.getWorldPosition().x + IChunk.WIDTH,
                        IChunk.HEIGHT,
                        chunk.getWorldPosition().y + IChunk.WIDTH
                ) || player.getChunkPosition().gridDistance(chunk.getPosition()) > 12) {
                    chunk.cleanup();
                    continue;
                }

                Mesh mesh = chunk.getChunkMesh(camera, atlas);
                mesh.render();

            }
        }
        shader.unbind();
        texture.unbind();
    }

    public void updateSelectedBlock(IWorld world, IPlayerEntity player) {
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