package ru.mrbedrockpy.renderer.world;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import ru.mrbedrockpy.craftengine.core.util.config.CraftEngineConfig;
import ru.mrbedrockpy.craftengine.core.util.id.RL;
import ru.mrbedrockpy.craftengine.core.world.chunk.Chunk;
import ru.mrbedrockpy.renderer.RenderInit;
import ru.mrbedrockpy.renderer.graphics.*;
import ru.mrbedrockpy.renderer.graphics.tex.Atlas;
import ru.mrbedrockpy.renderer.graphics.tex.GlTexture;
import ru.mrbedrockpy.renderer.util.graphics.ShaderUtil;
import ru.mrbedrockpy.renderer.util.graphics.TextureUtil;

import java.io.IOException;
import java.util.*;

import static org.lwjgl.opengl.GL46C.*;

public class WorldRenderer {
    @Getter
    private final Shader shader;
    private final TextureAtlas atlas;
    private final Texture texture;
    private final SkyboxRenderer skyboxRenderer;

    private final Map<Chunk, Mesh> posMeshes = new HashMap<>();
    private final Map<Vector2i, Chunk> chunksByPos = new HashMap<>();
    private final int BLOCKS_ATLAS;

    private final BlockReader blockReader = new BlockReader() {
        @Override
        public boolean isSolid(int wx, int wy, int wz) {
            int cx = Math.floorDiv(wx, Chunk.SIZE);
            int cy = Math.floorDiv(wy, Chunk.SIZE);
            Chunk ch = chunksByPos.get(new Vector2i(cx, cy));
            if (ch == null) return false;

            int lx = Math.floorMod(wx, Chunk.SIZE);
            int ly = Math.floorMod(wy, Chunk.SIZE);
            int lz = wz;
            if (lz < 0 || lz >= Chunk.SIZE) return true;

            return ch.getBlocks()[lx][ly][lz] != 0;
        }
    };

    public WorldRenderer() {
        shader = ShaderUtil.load("vertex.glsl", "fragment.glsl");
        this.atlas = new TextureAtlas(Chunk.SIZE);
        try {
            loadTextures();
        } catch (IOException e) {
            e.printStackTrace();
        }
        texture = atlas.buildAtlas();
        GlTexture blocksGl = new GlTexture(texture.getId(), texture.getWidth(), texture.getHeight());
        Atlas blocks = new Atlas(RL.of("blocks"), blocksGl, atlas);
        BLOCKS_ATLAS = RenderInit.ATLAS_MANAGER.register(blocks);
        skyboxRenderer = new SkyboxRenderer("skybox.png");
    }

    private void loadTextures() throws IOException {
        for (Map.Entry<RL, Texture> texture : RenderInit.RESOURCE_MANAGER.getTextureLoader().getAll()) {
            if (!texture.getKey().path().startsWith("block/")) continue;
            atlas.addTile(texture.getKey(), TextureUtil.toBufferedImage(texture.getValue()));
        }
    }

    private final FrustumCuller culler = new FrustumCuller();

    public void render(Vector2i playerPos, Matrix4f proj, Matrix4f view) {
        skyboxRenderer.render(proj, view);

        shader.use();
        shader.setUniformMatrix4f("projection", proj);

        RenderInit.ATLAS_MANAGER.uploadToShader(shader.getId(), "atlases");
        shader.setUniformMatrix4f("view", view);

        shader.setUniform1b("useMask", false);
        shader.setUniform1b("useUniformColor", false);
        shader.setUniform1b("useView", true);

        Matrix4f projView = new Matrix4f(proj).mul(view);
        culler.update(projView);

        // 3) Рендер чанков
        for (Chunk chunk : posMeshes.keySet()) {
            if (distanceByAxis(playerPos, chunk.getPosition()) > CraftEngineConfig.RENDER_DISTANCE
                    || !culler.isBoxVisible(chunk.getAABB())) continue;

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
}