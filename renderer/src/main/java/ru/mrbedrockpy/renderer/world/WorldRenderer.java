package ru.mrbedrockpy.renderer.world;

import org.joml.Matrix4f;
import org.joml.Vector2i;
import ru.mrbedrockpy.craftengine.core.world.chunk.Chunk;
import ru.mrbedrockpy.renderer.RenderInit;
import ru.mrbedrockpy.renderer.graphics.*;
import ru.mrbedrockpy.renderer.util.FileLoader;
import ru.mrbedrockpy.renderer.util.graphics.ShaderUtil;

import java.io.IOException;
import java.util.*;

public class WorldRenderer {
    private static final int CHUNK_SIZE = 16;

    private final Shader shader;
    private final TextureAtlas atlas;
    private final Texture texture;

    private final Map<Chunk, Mesh> posMeshes = new HashMap<>();
    private final Map<Vector2i, Chunk> chunksByPos = new HashMap<>();

    private final BlockReader blockReader = new BlockReader() {
        @Override public boolean isSolid(int wx, int wy, int wz) {
            int cx = Math.floorDiv(wx, CHUNK_SIZE);
            int cy = Math.floorDiv(wy, CHUNK_SIZE);
            Chunk ch = chunksByPos.get(new Vector2i(cx, cy));
            if (ch == null) return false;

            int lx = Math.floorMod(wx, CHUNK_SIZE);
            int ly = Math.floorMod(wy, CHUNK_SIZE);
            int lz = wz;
            if (lz < 0 || lz >= CHUNK_SIZE) return true;

            return ch.getBlocks()[lx][ly][lz] != 0;
        }
    };

    public WorldRenderer() {
        shader = ShaderUtil.load("vertex.glsl", "fragment.glsl");
        this.atlas = new TextureAtlas(CHUNK_SIZE);
        try { loadTextures(); } catch (IOException e) { e.printStackTrace(); }
        texture = atlas.buildAtlas();
    }

    private void loadTextures() throws IOException {
        atlas.addTile("dirt", FileLoader.loadImage("dirt.png"));
        atlas.addTile("stone", FileLoader.loadImage("stone.png"));
    }

    private final FrustumCuller culler = new FrustumCuller();

    public void render(Vector2i playerPos, Matrix4f proj, Matrix4f view) {
        Matrix4f projView = new Matrix4f(proj).mul(view);
        culler.update(projView);
        shader.use();
        shader.setUniformMatrix4f("projection", proj);
        shader.setUniformMatrix4f("view", view);
        texture.use();
        for (Chunk chunk : posMeshes.keySet()) {
            if (distanceByAxis(playerPos, chunk.getPosition()) > RenderInit.CONFIG.getInt("render.distance")
                    || !culler.isBoxVisible(chunk.getAABB())) continue;
            posMeshes.get(chunk).render();
        }
        shader.unbind();
        texture.unbind();
    }

    public void createChunk(Chunk chunk){
        deleteChunk(chunk);
        chunksByPos.put(chunk.getPosition(), chunk);

        Mesh.Data meshData = new MeshBuilder(atlas, blockReader).createChunk(chunk);
        posMeshes.put(chunk, new Mesh().data(meshData));
    }

    public void deleteChunk(Chunk chunk){
        posMeshes.remove(chunk);
        chunksByPos.remove(chunk.getPosition());
    }

    private int distanceByAxis(Vector2i pos1, Vector2i pos2) {
        return Math.max(Math.abs(pos1.x - pos2.x), Math.abs(pos1.y - pos2.y));
    }
}