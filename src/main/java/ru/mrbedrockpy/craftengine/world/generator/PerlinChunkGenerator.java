package ru.mrbedrockpy.craftengine.world.generator;

import ru.mrbedrockpy.craftengine.registry.Registries;
import ru.mrbedrockpy.craftengine.world.block.Blocks;
import ru.mrbedrockpy.craftengine.world.chunk.Chunk;
import ru.mrbedrockpy.craftengine.world.generator.perlin.FractalNoise;

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
        final int CD = Chunk.SIZE;

        int worldX0 = chunk.getPosition().x * CW;
        int worldZ0 = chunk.getPosition().y * CD;

        for (int x = 0; x < CW; x++)
            for (int y = 0; y < CH; y++)
                for (int z = 0; z < CD; z++)
                    chunk.setBlock(x, y, z, Blocks.AIR);

        for (int lx = 0; lx < CW; lx++) {
            for (int lz = 0; lz < CD; lz++) {
                int wx = worldX0 + lx;
                int wz = worldZ0 + lz;

                float n = heightNoise.fbm(wx, wz);
                int h = baseHeight + Math.round((n * 0.5f + 0.5f) * heightVariance);
                h = Math.max(0, Math.min(h, CH - 1));

                for (int y = 0; y <= h; y++) {
                    int id = (y == h)
                            ? (h >= seaLevel ? 2 : 1)
                            : (y >= h - 3 ? 2 : 1);
                    chunk.setBlock(lx, y, lz, Registries.BLOCKS.get(id));
                }

                if (h < seaLevel) {
                    for (int y = h + 1; y <= Math.min(seaLevel, CH - 1); y++) {
                        chunk.setBlock(lx, y, lz, Blocks.AIR);
                    }
                }
            }
        }
    }
}
