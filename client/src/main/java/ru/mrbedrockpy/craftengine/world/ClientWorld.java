package ru.mrbedrockpy.craftengine.world;

import ru.mrbedrockpy.craftengine.world.entity.ClientPlayerEntity;
import ru.mrbedrockpy.craftengine.world.generator.PerlinChunkGenerator;
import ru.mrbedrockpy.renderer.api.IChunk;
import ru.mrbedrockpy.renderer.world.WorldRenderer;

public class ClientWorld extends World {

    private final WorldRenderer worldRenderer;
    private final ClientPlayerEntity player;

    public ClientWorld(int size, ClientPlayerEntity player, TickSystem ticker) {
        super(size, new PerlinChunkGenerator(12345678L, 20, 30, 30));
        this.player = player;
        player.setWorld(this);
        addEntity(player);
        ticker.addListener(this::tick);
        this.worldRenderer = new WorldRenderer(player.getCamera());
    }

    public void render() {
        worldRenderer.render(this, player);
    }
}