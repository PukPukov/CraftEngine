package ru.mrbedrockpy.renderer.graphics;

import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.core.util.id.RL;
import ru.mrbedrockpy.craftengine.core.world.block.Block;
import ru.mrbedrockpy.craftengine.core.world.chunk.Chunk;
import ru.mrbedrockpy.renderer.RenderInit;
import ru.mrbedrockpy.renderer.graphics.model.Bone;
import ru.mrbedrockpy.renderer.graphics.model.Cuboid;
import ru.mrbedrockpy.renderer.graphics.model.Model;
import ru.mrbedrockpy.renderer.world.BlockReader;

import java.util.*;
import java.util.List;

public class MeshBuilder {

    private final List<Float> vertices = new ArrayList<>();
    private final List<Float> uvs = new ArrayList<>();

    private final TextureAtlas atlas;
    private final BlockReader reader;


    public MeshBuilder(TextureAtlas atlas, BlockReader reader) {
        this.atlas = atlas;
        this.reader = reader;
    }

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
                toFloatArray(uvs)
        );
    }

    private void v(float... pos) {
        vertices.add(pos[0]); vertices.add(pos[1]); vertices.add(pos[2]);
        uvs.add(pos[3]); uvs.add(pos[4]);
    }
    private static final int[][] VERTS = {
            {0,0,0}, // 0
            {1,0,0}, // 1
            {1,0,1}, // 2
            {0,0,1}, // 3
            {0,1,0}, // 4
            {1,1,0}, // 5
            {1,1,1}, // 6
            {0,1,1}, // 7
    };

    private static final int[][] FACE = new int[6][4];
    static {
        FACE[Block.Direction.EAST.ordinal()]  = new int[]{1, 2, 6, 5}; // +X
        FACE[Block.Direction.WEST.ordinal()]  = new int[]{3, 0, 4, 7}; // -X
        FACE[Block.Direction.UP.ordinal()]    = new int[]{7, 6, 5, 4}; // +Y
        FACE[Block.Direction.DOWN.ordinal()]  = new int[]{0, 1, 2, 3}; // -Y
        FACE[Block.Direction.NORTH.ordinal()] = new int[]{0, 4, 5, 1}; // -Z
        FACE[Block.Direction.SOUTH.ordinal()] = new int[]{2, 6, 7, 3}; // +Z
    }

    private void emitFace(int bx, int by, int bz,
                          Block.Direction d,
                          float[] uv4,
                          Chunk chunk) {

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

        // два треугольника: (0,1,2) и (0,2,3)
        emitCorner(baseX, baseY, baseZ, f[0], u0, v0);
        emitCorner(baseX, baseY, baseZ, f[1], u1, v1);
        emitCorner(baseX, baseY, baseZ, f[2], u2, v2);

        emitCorner(baseX, baseY, baseZ, f[0], u0, v0);
        emitCorner(baseX, baseY, baseZ, f[2], u2, v2);
        emitCorner(baseX, baseY, baseZ, f[3], u3, v3);
    }

    private void emitCorner(float baseX, float baseY, float baseZ, int vi, float u, float v) {
        int[] p = VERTS[vi];
        this.v(baseX + p[0], baseY + p[1], baseZ + p[2], u, v);
    }

    private boolean isSolidWorld(Chunk chunk, int lx, int ly, int lz) {
        Vector2i base = chunk.getWorldPosition();
        int wx = base.x + lx;
        int wy = ly;
        int wz = base.y + lz;
        return reader.isSolid(wx, wy, wz);
    }

    private static float[] toFloatArray(List<Float> src) {
        float[] a = new float[src.size()];
        for (int i = 0; i < src.size(); i++) a[i] = src.get(i);
        return a;
    }
}