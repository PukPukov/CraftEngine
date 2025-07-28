package ru.mrbedrockpy.craftengine.graphics;

import ru.mrbedrockpy.craftengine.registry.Registries;
import ru.mrbedrockpy.craftengine.world.block.Block;

import java.util.Arrays;

public class FaceData {
    // UDNSWE
    private static final float[][] FACE_VERTICES = {
        // UP (Y = 1)
        {
                0, 0, 0,
                1, 0, 0,
                1, 0, 1,
                0, 0, 1
        },
        // DOWN (Y = 0)
        {
                0, 0, 0,
                1, 0, 0,
                1, 1, 0,
                0, 1, 0
        },
        // NORTH (Z = 0)
        {
                0, 0, 1,
                1, 0, 1,
                1, 1, 1,
                0, 1, 1
        },
        // SOUTH (Z = 1)
        {
                0, 0, 0,
                0, 0, 1,
                0, 1, 1,
                0, 1, 0
        },
        // WEST (X = 0)
        {
                0, 1, 0,
                1, 1, 0,
                1, 1, 1,
                0, 1, 1
        },
        // EAST (X = 1)
        {
                1, 0, 0,
                1, 0, 1,
                1, 1, 1,
                1, 1, 0
        },
    };

    public static final float[][] FACE_UVS = {
            // DOWN
            {
                    0, 0,
                    1, 0,
                    1, 1,
                    0, 1
            },
            // UP
            {
                    0, 0,
                    1, 0,
                    1, 1,
                    0, 1
            },
            // NORTH
            {
                    0, 0,
                    1, 0,
                    1, 1,
                    0, 1
            },
            // SOUTH
            {
                    0, 0,
                    1, 0,
                    1, 1,
                    0, 1
            },
            // WEST
            {
                    0, 0,
                    1, 0,
                    1, 1,
                    0, 1
            },
            // EAST
            {
                    0, 0,
                    1, 0,
                    1, 1,
                    0, 1
            },
    };

    public static float[] getVertices(Block.Direction dir, int x, int y, int z) {
        float[] base = FACE_VERTICES[dir.ordinal()];
        float[] out = new float[base.length];

        for (int i = 0; i < base.length / 3; i++) {
            out[i * 3]     = base[i * 3]     + x;
            out[i * 3 + 1] = base[i * 3 + 1] + y;
            out[i * 3 + 2] = base[i * 3 + 2] + z;
        }

        return out;
    }

    public static float[] getUVs(Block block, Block.Direction dir, TextureAtlas atlas) {
        return atlas.getNormalizedUV(Registries.BLOCKS.getName(block), dir);
    }
}