package ru.mrbedrockpy.craftengine.render.world;

import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.render.RenderInit;
import ru.mrbedrockpy.craftengine.render.Shaders;
import ru.mrbedrockpy.craftengine.render.graphics.*;
import ru.mrbedrockpy.craftengine.util.config.CraftEngineConfig;
import ru.mrbedrockpy.craftengine.world.chunk.Chunk;

import java.util.*;

import static org.lwjgl.opengl.GL46C.*;

public class WorldRenderer implements AutoCloseable {

    private TextureAtlas atlas;
    private Texture texture;
    private SkyboxRenderer skyboxRenderer;

    private final Map<Chunk, Mesh> posMeshes = new HashMap<>();
    private final Map<Vector2i, Chunk> chunksByPos = new HashMap<>();
    private final int BLOCKS_ATLAS;

    private final FrustumCuller culler = new FrustumCuller();

    public WorldRenderer() {
        this.atlas = RenderInit.blocksAtlasBuilder();
        texture = atlas.buildAtlas();
        BLOCKS_ATLAS = RenderInit.blocksAtlasIndex();
        skyboxRenderer = new SkyboxRenderer("skybox.png");
    }

    public void render(long tick, Vector2i playerPos, Matrix4f proj, Matrix4f view) {
        skyboxRenderer.render(proj, view);

        Shaders.BLOCK_SHADER.use();
        Shaders.BLOCK_SHADER.setUniformMatrix4f("projection", proj);
        Shaders.BLOCK_SHADER.setUniformMatrix4f("view", view);
        Shaders.BLOCK_SHADER.setUniformMatrix4f("model", new Matrix4f());

        Vector3f lightPos = new Vector3f(-10, 1.0f, 5);
//        Logger.getLogger(WorldRenderer.class).info("Rendering world! Light: " + lightPos);
        Shaders.BLOCK_SHADER.setUniform3f("lightDir", lightPos.x, lightPos.y, lightPos.z);
        Shaders.BLOCK_SHADER.setUniform3f("lightColor", 1f, 1f, 1f);

        RenderInit.ATLAS_MANAGER.uploadToShader(Shaders.BLOCK_SHADER.getId(), "atlases");

        Matrix4f projView = new Matrix4f(proj).mul(view);
        culler.update(projView);

        for (Chunk chunk : posMeshes.keySet()) {
            if (distanceByAxis(playerPos, chunk.getPosition()) > CraftEngineConfig.RENDER_DISTANCE || !culler.isBoxVisible(chunk.getAABB())) continue;

            Mesh mesh = posMeshes.get(chunk);

            mesh.render(mesh1 -> {
                glDisableVertexAttribArray(2);
                glVertexAttrib4f(2, 1f, 1f, 1f, 1f);
                glDisableVertexAttribArray(3);
                glVertexAttribI1i(3, BLOCKS_ATLAS);

                glDrawArrays(GL_TRIANGLES, 0, mesh.getVertexCount());
            });
        }
    }

    public void createChunk(Chunk chunk) {
        deleteChunk(chunk);
        chunksByPos.put(chunk.getPosition(), chunk);
        posMeshes.put(chunk, Mesh.fromData(new MeshBuilder(atlas).createChunk(chunk)));
    }

    public void deleteChunk(Chunk chunk) {
        posMeshes.remove(chunk);
        chunksByPos.remove(chunk.getPosition());
    }

    private int distanceByAxis(Vector2i pos1, Vector2i pos2) {
        return Math.max(Math.abs(pos1.x - pos2.x), Math.abs(pos1.y - pos2.y));
    }

    @Override
    public void close() {
        for (Mesh m : posMeshes.values()) {
            try { m.close(); } catch (Exception ignored) {}
        }
        posMeshes.clear();
        chunksByPos.clear();

        if (skyboxRenderer != null) {
            try { skyboxRenderer.close(); } catch (Exception ignored) {}
            skyboxRenderer = null;
        }

        if (texture != null) {
            try { texture.close(); } catch (Exception ignored) {}
            texture = null;
        }

        atlas = null;
    }
}