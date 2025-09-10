package ru.mrbedrockpy.craftengine.world;

import org.joml.Vector2i;
import ru.mrbedrockpy.craftengine.world.entity.ClientPlayerEntity;
import ru.mrbedrockpy.craftengine.world.generator.PerlinChunkGenerator;
import ru.mrbedrockpy.craftengine.world.generator.SimpleChunkGenerator;
import ru.mrbedrockpy.renderer.api.IBlock;
import ru.mrbedrockpy.renderer.api.RenderChunk;
import ru.mrbedrockpy.renderer.world.WorldRenderer;

import java.util.concurrent.ThreadLocalRandom;

public class ClientWorld extends World {

    private final WorldRenderer worldRenderer;
    private final ClientPlayerEntity player;

    public ClientWorld(int size, ClientPlayerEntity player, TickSystem ticker) {
        super(size, new PerlinChunkGenerator(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE), 5, 5, 10));
        this.player = player;
        player.setWorld(this);
        addEntity(player);
        ticker.addListener(this::tick);
        this.worldRenderer = new WorldRenderer();
        chunkLoadManager.subscribeOnLoad(this::createRenderChunk);
        chunkLoadManager.subscribeOnUnload(this::removeRenderChunk);
    }

    public void render() {
        worldRenderer.render(player.getChunkPosition(), player.getCamera().getProjectionMatrix(), player.getCamera().getViewMatrix());
    }

    private static final int CHUNK_SIZE = 16;

    @Override
    public boolean setBlock(int x, int y, int z, IBlock block) {
        boolean success = super.setBlock(x, y, z, block);

        int cx = Math.floorDiv(x, CHUNK_SIZE);
        int cy = Math.floorDiv(y, CHUNK_SIZE);
        int lx = Math.floorMod(x, CHUNK_SIZE);
        int ly = Math.floorMod(y, CHUNK_SIZE);

        createRenderChunk(new Vector2i(cx, cy));

        if (lx == 0)              createRenderChunk(new Vector2i(cx - 1, cy));
        if (lx == CHUNK_SIZE - 1) createRenderChunk(new Vector2i(cx + 1, cy));

        if (ly == 0)              createRenderChunk(new Vector2i(cx, cy - 1));
        if (ly == CHUNK_SIZE - 1) createRenderChunk(new Vector2i(cx, cy + 1));

        if (lx == 0 && ly == 0)                              createRenderChunk(new Vector2i(cx - 1, cy - 1));
        if (lx == 0 && ly == CHUNK_SIZE - 1)                 createRenderChunk(new Vector2i(cx - 1, cy + 1));
        if (lx == CHUNK_SIZE - 1 && ly == 0)                 createRenderChunk(new Vector2i(cx + 1, cy - 1));
        if (lx == CHUNK_SIZE - 1 && ly == CHUNK_SIZE - 1)    createRenderChunk(new Vector2i(cx + 1, cy + 1));

        return success;
    }

    private void createRenderChunk(Vector2i pos){
        if(pos.x >= 0 && pos.y >= 0) {
            worldRenderer.createChunk(new RenderChunk(pos, chunk(pos).getBlocks()));
        }
    }

    private void removeRenderChunk(Vector2i pos){
        if(pos.x >= 0 && pos.y >= 0) {
            worldRenderer.deleteChunk(new RenderChunk(pos, chunk(pos).getBlocks()));
        }
    }
}