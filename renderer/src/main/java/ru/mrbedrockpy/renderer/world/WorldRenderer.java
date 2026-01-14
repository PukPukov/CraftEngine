package ru.mrbedrockpy.renderer.world;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import ru.mrbedrockpy.craftengine.core.util.config.CraftEngineConfig;
import ru.mrbedrockpy.craftengine.core.world.chunk.Chunk;
import ru.mrbedrockpy.renderer.RenderInit;
import ru.mrbedrockpy.renderer.graphics.*;
import ru.mrbedrockpy.renderer.util.graphics.ShaderUtil;

import java.util.*;

import static org.lwjgl.opengl.GL46C.*;

public class WorldRenderer implements AutoCloseable {
    @Getter
    private Shader shader;
    private TextureAtlas atlas;
    private Texture texture;
    private SkyboxRenderer skyboxRenderer;

    private final Map<Chunk, Mesh> posMeshes = new HashMap<>();
    private final Map<Vector2i, Chunk> chunksByPos = new HashMap<>();
    private final int BLOCKS_ATLAS;

    private final BlockReader blockReader = new BlockReader() {
        @Override
        public boolean isSolid(int wx, int wy, int wz) {
            int cx = Math.floorDiv(wx, Chunk.SIZE);
            int cz = Math.floorDiv(wz, Chunk.SIZE);
            Chunk ch = chunksByPos.get(new Vector2i(cx, cz));
            if (ch == null) return false;

            int lx = Math.floorMod(wx, Chunk.SIZE);
            int ly = wy;
            int lz = Math.floorMod(wz, Chunk.SIZE);
            if (ly < 0 || ly >= Chunk.SIZE) return false;

            return ch.getBlocks()[lx][ly][lz] != 0;
        }
    };

    public WorldRenderer() {
        shader = ShaderUtil.load("vertex.glsl", "fragment.glsl");
        this.atlas = RenderInit.blocksAtlasBuilder();
        texture = atlas.buildAtlas();
        BLOCKS_ATLAS = RenderInit.blocksAtlasIndex();
        skyboxRenderer = new SkyboxRenderer("skybox.png");
    }


    private final FrustumCuller culler = new FrustumCuller();

    public void render(Vector2i playerPos, Matrix4f proj, Matrix4f view) {
        skyboxRenderer.render(proj, view);

        shader.use();
        shader.setUniformMatrix4f("projection", proj);
        shader.setUniformMatrix4f("view", view);
        shader.setUniformMatrix4f("model", new Matrix4f());

        shader.setUniform1b("useMask", false);

        RenderInit.ATLAS_MANAGER.uploadToShader(shader.getId(), "atlases");

        Matrix4f projView = new Matrix4f(proj).mul(view);
        culler.update(projView);

        for (Chunk chunk : posMeshes.keySet()) {
            if (distanceByAxis(playerPos, chunk.getPosition()) > CraftEngineConfig.RENDER_DISTANCE
                    || !culler.isBoxVisible(chunk.getAABB())
            ) continue;

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

        Mesh.Data meshData = new MeshBuilder(atlas, blockReader).createChunk(chunk);
        posMeshes.put(chunk, new Mesh().data(meshData));
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

        if (shader != null) {
            try { shader.close(); } catch (Exception ignored) {}
            shader = null;
        }

        atlas = null;
    }
}