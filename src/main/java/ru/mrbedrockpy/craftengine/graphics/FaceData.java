package ru.mrbedrockpy.craftengine.graphics;

import ru.mrbedrockpy.craftengine.world.block.Block;

public class FaceData {

    private static final float[][] FACE_VERTICES = {
            // UP (Y = 1)
            {
                    0, 1, 0,
                    1, 1, 1,
                    1, 1, 0,
                    0, 1, 0,
                    0, 1, 1,
                    1, 1, 1
            },
            // DOWN (Y = 0)
            {
                    0, 0, 0,
                    1, 0, 0,
                    1, 0, 1,
                    0, 0, 0,
                    1, 0, 1,
                    0, 0, 1
            },
            // NORTH
            {
                    0, 0, 0,
                    1, 1, 0,
                    1, 0, 0,
                    0, 0, 0,
                    0, 1, 0,
                    1, 1, 0
            },
            // SOUTH
            {
                    0, 0, 1,
                    1, 0, 1,
                    1, 1, 1,
                    0, 0, 1,
                    1, 1, 1,
                    0, 1, 1
            },
            // WEST
            {
                    0, 0, 0,
                    0, 0, 1,
                    0, 1, 1,
                    0, 0, 0,
                    0, 1, 1,
                    0, 1, 0
            },
            // EAST
            {
                    1, 0, 0,
                    1, 1, 1,
                    1, 0, 1,
                    1, 0, 0,
                    1, 1, 0,
                    1, 1, 1
            }
    };

    private static final float[][] FACE_UVS = {
            // UP
            {
                    0, 0,
                    1, 1,
                    1, 0,
                    0, 0,
                    0, 1,
                    1, 1
            },
            // DOWN
            {
                    0, 0,
                    1, 0,
                    1, 1,
                    0, 0,
                    1, 1,
                    0, 1
            },
            // NORTH
            {
                    0, 0,
                    1, 0,
                    1, 1,
                    0, 0,
                    1, 1,
                    0, 1
            },
            // SOUTH
            {
                    0, 0,
                    1, 1,
                    1, 0,
                    0, 0,
                    0, 1,
                    1, 1
            },
            // WEST
            {
                    0, 0,
                    1, 1,
                    1, 0,
                    0, 0,
                    0, 1,
                    1, 1
            },
            // EAST
            {
                    0, 0,
                    1, 0,
                    1, 1,
                    0, 0,
                    1, 1,
                    0, 1
            }
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

    public static float[] getUVs(Block block, Block.Direction dir) {
        return FACE_UVS[dir.ordinal()];
    }
}