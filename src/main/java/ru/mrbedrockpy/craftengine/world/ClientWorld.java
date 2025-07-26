package ru.mrbedrockpy.craftengine.world;

import ru.mrbedrockpy.craftengine.world.entity.ClientPlayerEntity;
import ru.mrbedrockpy.craftengine.world.generator.SimpleChunkGenerator;

public class ClientWorld extends World {

    private final WorldRenderer worldRenderer;
    private final ClientPlayerEntity player;

    public ClientWorld(int size, ClientPlayerEntity player, TickSystem ticker) {
        super(size, new SimpleChunkGenerator());
        this.player = player;
        ticker.addListener(this::tick);
        this.worldRenderer = new WorldRenderer(player.getCamera(), getWorldSize(), Chunk.HEIGHT, getWorldSize());
    }

    public void render() {
        worldRenderer.render(this, player);
    }
}
