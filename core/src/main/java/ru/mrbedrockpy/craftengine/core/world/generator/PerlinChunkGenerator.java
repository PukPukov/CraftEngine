package ru.mrbedrockpy.craftengine.core.world.generator;

import ru.mrbedrockpy.craftengine.core.registry.Registries;
import ru.mrbedrockpy.craftengine.core.world.block.Blocks;
import ru.mrbedrockpy.craftengine.core.world.chunk.Chunk;
import ru.mrbedrockpy.craftengine.core.world.generator.perlin.FractalNoise;

public final class PerlinChunkGenerator implements ChunkGenerator {
    private final FractalNoise heightNoise;
    private final int seaLevel;
    private final int baseHeight;
    private final int heightVariance;
    public static PerlinChunkGenerator DEFAULT = new PerlinChunkGenerator(0L, 5, 5, 10);

    public PerlinChunkGenerator(long seed, int seaLevel, int baseHeight, int heightVariance) {
        this.heightNoise = new FractalNoise(seed);
        this.seaLevel = seaLevel;
        this.baseHeight = baseHeight;
        this.heightVariance = heightVariance;

        heightNoise.scale = 0.01f;
        heightNoise.octaves = 5;
        heightNoise.lacunarity = 2.0f;
        heightNoise.gain = 0.5f;
    }

    @Override
    public void generate(Chunk chunk) {
        final int CW = Chunk.SIZE;
        final int CH = Chunk.SIZE;

        int worldX0 = chunk.getPosition().x * CW;
        int worldY0 = chunk.getPosition().y * CW;

        for (int x = 0; x < CW; x++) for (int y = 0; y < CW; y++) for (int z = 0; z < CH; z++)
            chunk.setBlock(x, y, z, Blocks.AIR);

        for (int lx = 0; lx < CW; lx++) {
            for (int ly = 0; ly < CW; ly++) {
                int wx = worldX0 + lx;
                int wy = worldY0 + ly;

                float n = heightNoise.fbm(wx, wy);
                int h = baseHeight + Math.round((n * 0.5f + 0.5f) * heightVariance);
                h = Math.max(0, Math.min(h, CH - 1));

                for (int z = 0; z <= h; z++) {
                    int id = (z == h)
                        ? (h >= seaLevel ? 2 : 1)
                        : (z >= h - 3 ? 2 : 1);
                    chunk.setBlock(lx, z, ly, Registries.BLOCKS.get(id));
                }

                if (h < seaLevel) {
                    for (int z = h + 1; z <= Math.min(seaLevel, CH - 1); z++) {
                        chunk.setBlock(lx, z, ly, Blocks.AIR);
                    }
                }
            }
        }
    }
}