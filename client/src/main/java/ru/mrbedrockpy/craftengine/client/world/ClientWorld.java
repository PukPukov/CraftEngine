package ru.mrbedrockpy.craftengine.client.world;

import org.joml.Vector2i;
import ru.mrbedrockpy.craftengine.client.world.entity.ClientPlayerEntity;
import ru.mrbedrockpy.craftengine.core.world.block.Block;
import ru.mrbedrockpy.craftengine.core.world.chunk.Chunk;
import ru.mrbedrockpy.craftengine.core.world.generator.PerlinChunkGenerator;
import ru.mrbedrockpy.craftengine.core.world.World;
import ru.mrbedrockpy.renderer.world.WorldRenderer;

public class ClientWorld extends World {

    private final WorldRenderer worldRenderer = new WorldRenderer();
    private final ClientPlayerEntity player;

    public ClientWorld(int size, ClientPlayerEntity player) {
        super(size, PerlinChunkGenerator.DEFAULT);
        this.player = player;
        player.setWorld(this);
        addEntity(player);
        chunkLoadManager.subscribeOnLoad(this::createRenderChunk);
        chunkLoadManager.subscribeOnUnload(this::removeRenderChunk);
    }

    public ClientWorld(World base, ClientPlayerEntity player){
        this(base.getSize(), player);
        for(Chunk[] chunks : base.getChunks()){
            for (Chunk chunk : chunks){
                this.setChunk(chunk);
            }
        }
    }

    public void render() {
        worldRenderer.render(player.getChunkPosition(), player.getCamera().getProjectionMatrix(), player.getCamera().getViewMatrix());
    }

    private static final int CHUNK_SIZE = 16;

    @Override
    public boolean setBlock(int x, int y, int z, Block block) {
        boolean success = super.setBlock(x, y, z, block);

        int cx = Math.floorDiv(x, CHUNK_SIZE);
        int cy = Math.floorDiv(y, CHUNK_SIZE);
        int lx = Math.floorMod(x, CHUNK_SIZE);
        int ly = Math.floorMod(y, CHUNK_SIZE);

        createRenderChunk(new Vector2i(cx, cy));

        if (lx == 0) createRenderChunk(new Vector2i(cx - 1, cy));
        if (lx == CHUNK_SIZE - 1) createRenderChunk(new Vector2i(cx + 1, cy));

        if (ly == 0) createRenderChunk(new Vector2i(cx, cy - 1));
        if (ly == CHUNK_SIZE - 1) createRenderChunk(new Vector2i(cx, cy + 1));

        if (lx == 0 && ly == 0) createRenderChunk(new Vector2i(cx - 1, cy - 1));
        if (lx == 0 && ly == CHUNK_SIZE - 1) createRenderChunk(new Vector2i(cx - 1, cy + 1));
        if (lx == CHUNK_SIZE - 1 && ly == 0) createRenderChunk(new Vector2i(cx + 1, cy - 1));
        if (lx == CHUNK_SIZE - 1 && ly == CHUNK_SIZE - 1) createRenderChunk(new Vector2i(cx + 1, cy + 1));

        return success;
    }

    @Override
    public void setChunk(Chunk chunk) {
        super.setChunk(chunk);
        if(chunk != null) createRenderChunk(chunk.getPosition());
    }

    private void createRenderChunk(Vector2i pos) {
        if (pos.x >= 0 && pos.y >= 0) {
            Chunk chunk = getChunk(pos);
            if (chunk != null) worldRenderer.createChunk(chunk);
        }
    }

    private void removeRenderChunk(Vector2i pos) {
        if (pos.x >= 0 && pos.y >= 0) {
            Chunk chunk = getChunk(pos);
            if (chunk != null) worldRenderer.deleteChunk(chunk);
        }
    }
}