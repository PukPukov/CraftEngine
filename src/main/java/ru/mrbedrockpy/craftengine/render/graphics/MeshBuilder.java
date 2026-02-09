package ru.mrbedrockpy.craftengine.render.graphics;

import lombok.RequiredArgsConstructor;
import org.joml.Vector2i;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.render.RenderInit;
import ru.mrbedrockpy.craftengine.util.id.RL;
import ru.mrbedrockpy.craftengine.world.block.Block;
import ru.mrbedrockpy.craftengine.world.chunk.Chunk;

import java.util.*;
import java.util.List;

@RequiredArgsConstructor
public class MeshBuilder {

    private final List<Float> vertices = new ArrayList<>();
    private final List<Float> uvs = new ArrayList<>();
    private final List<Float> normals = new ArrayList<>();

    private final TextureAtlas atlas;

    private static final int[][] VERTICES = {
            {0,0,0}, // 0
            {1,0,0}, // 1
            {1,0,1}, // 2
            {0,0,1}, // 3
            {0,1,0}, // 4
            {1,1,0}, // 5
            {1,1,1}, // 6
            {0,1,1}, // 7
    };

    private static final float[][] FACE_NORMALS = {
            { 1, 0, 0}, // EAST
            {-1, 0, 0}, // WEST
            { 0, 1, 0}, // UP
            { 0,-1, 0}, // DOWN
            { 0, 0,-1}, // NORTH
            { 0, 0, 1}  // SOUTH
    };

    private static final int[][] FACE = new int[6][4];

    public Mesh.Data createChunk(Chunk chunk) {
        vertices.clear();
        uvs.clear();

        for (int y = 0; y < Chunk.SIZE; y++) {
            for (int z = 0; z < Chunk.SIZE; z++) {
                for (int x = 0; x < Chunk.SIZE; x++) {
                    short id = chunk.getBlocks()[x][y][z];
                    if (id == 0) continue;

                    for (Block.Direction d : Block.Direction.getValues()) {
                        Vector3i n = d.offset();
                        if (isSolidWorld(chunk, x + n.x, y + n.y, z + n.z)) continue;

                        RL block = RenderInit.BLOCKS.getRL(RenderInit.BLOCKS.get(id));
                        float[] uv4 = atlas.getNormalizedUvs(RL.of(block.namespace(), "block/" + block.path()));

                        emitFace(x, y, z, d, uv4, chunk);
                    }
                }
            }
        }

        return new Mesh.Data(
                toFloatArray(vertices),
                toFloatArray(uvs),
                toFloatArray(normals)
        );
    }

    private void v(float... pos) {
        vertices.add(pos[0]); vertices.add(pos[1]); vertices.add(pos[2]);
        uvs.add(pos[3]); uvs.add(pos[4]);
    }

    static {
        FACE[Block.Direction.EAST.ordinal()]  = new int[]{1, 2, 6, 5}; // +X
        FACE[Block.Direction.WEST.ordinal()]  = new int[]{3, 0, 4, 7}; // -X
        FACE[Block.Direction.UP.ordinal()]    = new int[]{7, 6, 5, 4}; // +Y
        FACE[Block.Direction.DOWN.ordinal()]  = new int[]{0, 1, 2, 3}; // -Y
        FACE[Block.Direction.NORTH.ordinal()] = new int[]{0, 4, 5, 1}; // -Z
        FACE[Block.Direction.SOUTH.ordinal()] = new int[]{2, 6, 7, 3}; // +Z
    }

    private void emitFace(int bx, int by, int bz, Block.Direction d, float[] uv4, Chunk chunk) {
        if (d == Block.Direction.NONE) return;

        Vector2i wp = chunk.getWorldPosition();
        float baseX = bx + wp.x;
        float baseY = by;
        float baseZ = bz + wp.y;

        // UV углы (0..3)
        float u0 = uv4[0], v0 = uv4[1];
        float u1 = uv4[2], v1 = uv4[3];
        float u2 = uv4[4], v2 = uv4[5];
        float u3 = uv4[6], v3 = uv4[7];

        // индексы 4 углов грани
        int[] f = FACE[d.ordinal()];

        float[] n = FACE_NORMALS[d.ordinal()];

        emitCorner(baseX, baseY, baseZ, f[0], u0, v0, n[0], n[1], n[2]);
        emitCorner(baseX, baseY, baseZ, f[1], u1, v1, n[0], n[1], n[2]);
        emitCorner(baseX, baseY, baseZ, f[2], u2, v2, n[0], n[1], n[2]);

        emitCorner(baseX, baseY, baseZ, f[0], u0, v0, n[0], n[1], n[2]);
        emitCorner(baseX, baseY, baseZ, f[2], u2, v2, n[0], n[1], n[2]);
        emitCorner(baseX, baseY, baseZ, f[3], u3, v3, n[0], n[1], n[2]);
    }

    private void emitCorner(float baseX, float baseY, float baseZ,
                            int vi, float u, float v,
                            float nx, float ny, float nz) {

        int[] p = VERTICES[vi];
        vertices.add(baseX + p[0]);
        vertices.add(baseY + p[1]);
        vertices.add(baseZ + p[2]);

        uvs.add(u);
        uvs.add(v);

        normals.add(nx);
        normals.add(ny);
        normals.add(nz);
    }


    private boolean isSolidWorld(Chunk chunk, int lx, int ly, int lz) {
        if (lx < 0 || lx >= Chunk.SIZE) return false;
        if (ly < 0 || ly >= Chunk.SIZE) return false;
        if (lz < 0 || lz >= Chunk.SIZE) return false;
        return chunk.getBlocks()[lx][ly][lz] != 0;
    }

    private static float[] toFloatArray(List<Float> src) {
        float[] a = new float[src.size()];
        for (int i = 0; i < src.size(); i++) a[i] = src.get(i);
        return a;
    }
}