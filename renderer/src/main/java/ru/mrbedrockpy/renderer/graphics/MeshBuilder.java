package ru.mrbedrockpy.renderer.graphics;

import com.google.gson.JsonObject;
import org.joml.Vector2i;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.core.world.block.Block;
import ru.mrbedrockpy.craftengine.core.world.chunk.Chunk;
import ru.mrbedrockpy.renderer.RenderInit;
import ru.mrbedrockpy.renderer.util.graphics.MeshUtil;
import ru.mrbedrockpy.renderer.world.BlockReader;

import java.util.*;
import java.util.List;

public class MeshBuilder {

    private final List<Float> vertices = new ArrayList<>();
    private final List<Float> texCoords = new ArrayList<>();
    private final List<Float> aoValues  = new ArrayList<>();

    private final TextureAtlas atlas;
    private final BlockReader reader;

    private static final int SIZE = 16;

    public MeshBuilder(TextureAtlas atlas, BlockReader reader) {
        this.atlas = atlas;
        this.reader = reader;
    }

    public Mesh.Data createChunk(Chunk chunk) {
        vertices.clear();
        texCoords.clear();
        aoValues.clear();

        float[][][] faceCorners = getCornersForModel("cube_all");

        for (int z = 0; z < SIZE; z++) {
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    short id = chunk.getBlocks()[x][y][z];
                    if (id == 0) continue;

                    for (Block.Direction d : Block.Direction.getValues()) {
                        Vector3i n = d.offset();
                        if (isSolidWorld(chunk,x + n.x, y + n.y, z + n.z)) continue;

                        String tileName = RenderInit.BLOCKS.getName(RenderInit.BLOCKS.get(id));
                        float[] uv = atlas.normalizedUv(tileName);

                        float[][] corners = faceCorners[d.ordinal()];

                        emitFace(x, y, z, d, corners, uv, chunk);
                    }
                }
            }
        }

        return new Mesh.Data(
                toFloatArray(vertices),
                toFloatArray(texCoords),
                toFloatArray(aoValues)
        );
    }

    private void v(float[] pos, float[] uv, float ao) {
        vertices.add(pos[0]); vertices.add(pos[1]); vertices.add(pos[2]);
        texCoords.add(uv[0]); texCoords.add(uv[1]);
        aoValues.add(ao);
    }

    private void emitFace(int bx, int by, int bz,
                          Block.Direction d,
                          float[][] corners,
                          float[] uv4,
                          Chunk chunk) {

        float[] aoCorner = computeFaceAO(chunk, bx, by, bz, d, corners);

        float s02 = aoCorner[0] + aoCorner[2];
        float s13 = aoCorner[1] + aoCorner[3];

        final int[][] TRI_A = {{0,2,1},{0,3,2}};
        final int[][] TRI_B = {{0,1,3},{3,1,2}};
        int[][] tri = (s02 > s13) ? TRI_A : TRI_B;
        float[][] uvByCorner = {
                {uv4[0], uv4[1]},
                {uv4[2], uv4[3]},
                {uv4[4], uv4[5]},
                {uv4[6], uv4[7]}
        };

        for (int t = 0; t < 2; t++) {
            for (int k = 0; k < 3; k++) {
                int c = tri[t][k];
                float[] p = corners[c];
                float[] pos = new float[]{ p[0] + bx + chunk.getWorldPosition().x, p[1] + by + chunk.getWorldPosition().y, p[2] + bz };
                v(pos, uvByCorner[c], aoCorner[c]);
            }
        }
    }

    private static float[] toFloatArray(List<Float> src) {
        float[] a = new float[src.size()];
        for (int i = 0; i < src.size(); i++) a[i] = src.get(i);
        return a;
    }

    private float[] computeFaceAO(Chunk chunk, int x, int y, int z,
                                  Block.Direction face, float[][] corners) {
        int dir = face.ordinal();
        float[] out = new float[4];

        for (int i = 0; i < 4; i++) {
            int[] o1 = AO_OFFSETS[dir][i][0];
            int[] o2 = AO_OFFSETS[dir][i][1];
            int[] o3 = AO_OFFSETS[dir][i][2];

            boolean side1 = isSolidWorld(chunk, x + o1[0], y + o1[1], z + o1[2]);
            boolean side2 = isSolidWorld(chunk, x + o2[0], y + o2[1], z + o2[2]);
            boolean diag  = isSolidWorld(chunk, x + o3[0], y + o3[1], z + o3[2]);

            out[i] = aoValue(side1, side2, diag);
        }
        return out;
    }

    private float[][][] getCornersForModel(String modelName) {
        int LEN = Block.Direction.getValues().size();
        float[][][] out = new float[LEN][4][3];

        JsonObject model = RenderInit.RESOURCE_MANAGER.getModel("cube_all");
        if (model == null) return out;

        for (Block.Direction d : Block.Direction.getValues()) {
            JsonObject face = model.has(d.name()) && model.get(d.name()).isJsonObject()
                    ? model.getAsJsonObject(d.name())
                    : (model.has(d.name().toLowerCase()) && model.get(d.name().toLowerCase()).isJsonObject()
                    ? model.getAsJsonObject(d.name().toLowerCase())
                    : null);
            if (face == null || !face.has("vertices") || !face.get("vertices").isJsonArray()) continue;

            out[d.ordinal()] = MeshUtil.parseVerts(face.getAsJsonArray("vertices")); // 4x3
        }
        return out;
    }

    private boolean isSolidWorld(Chunk chunk, int lx, int ly, int lz) {
        Vector2i base = chunk.getWorldPosition();
        int wx = base.x + lx;
        int wy = base.y + ly;
        int wz = lz;
        return reader.isSolid(wx, wy, wz);
    }

    private static float aoValue(boolean side1, boolean side2, boolean corner){
        if (side1 && side2) return 0.5f;
        int occ = (side1?1:0) + (side2?1:0) + (corner?1:0);
        return switch (occ){
            case 0 -> 1.0f;
            case 1 -> 0.8f;
            case 2 -> 0.65f;
            default -> 0.5f;
        };
    }

    private static final int[][][][] AO_OFFSETS = {
            // UP (+Z)
            {
                    {{ 0,-1, 1}, {-1, 0, 1}, {-1,-1, 1}}, // corner 0
                    {{ 0,-1, 1}, { 1, 0, 1}, { 1,-1, 1}}, // corner 1
                    {{ 0, 1, 1}, { 1, 0, 1}, { 1, 1, 1}}, // corner 2
                    {{ 0, 1, 1}, {-1, 0, 1}, {-1, 1, 1}}, // corner 3
            },
            // DOWN (-Z)
            {
                    {{ 0, 1,-1}, {-1, 0,-1}, {-1, 1,-1}}, // c0
                    {{ 0, 1,-1}, { 1, 0,-1}, { 1, 1,-1}}, // c1
                    {{ 0,-1,-1}, { 1, 0,-1}, { 1,-1,-1}}, // c2
                    {{ 0,-1,-1}, {-1, 0,-1}, {-1,-1,-1}}, // c3
            },
            // NORTH (-Y)
            {
                    {{ 0,-1,-1}, {-1,-1, 0}, {-1,-1,-1}}, // c0
                    {{ 0,-1,-1}, { 1,-1, 0}, { 1,-1,-1}}, // c1
                    {{ 0,-1, 1}, { 1,-1, 0}, { 1,-1, 1}}, // c2
                    {{ 0,-1, 1}, {-1,-1, 0}, {-1,-1, 1}}, // c3
            },
            // SOUTH (+Y)
            {
                    {{ 0, 1,-1}, { 1, 1, 0}, { 1, 1,-1}}, // c0
                    {{ 0, 1,-1}, {-1, 1, 0}, {-1, 1,-1}}, // c1
                    {{ 0, 1, 1}, {-1, 1, 0}, {-1, 1, 1}}, // c2
                    {{ 0, 1, 1}, { 1, 1, 0}, { 1, 1, 1}}, // c3
            },
            // WEST (-X)
            {
                    {{-1, 1,-1}, {-1, 0, 0}, {-1, 1, 0}}, // c0
                    {{-1,-1,-1}, {-1, 0, 0}, {-1,-1, 0}}, // c1
                    {{-1,-1, 1}, {-1, 0, 0}, {-1,-1, 0}}, // c2
                    {{-1, 1, 1}, {-1, 0, 0}, {-1, 1, 0}}, // c3
            },
            // EAST (+X)
            {
                    {{ 1,-1,-1}, { 1, 0, 0}, { 1,-1, 0}}, // c0
                    {{ 1, 1,-1}, { 1, 0, 0}, { 1, 1, 0}}, // c1
                    {{ 1, 1, 1}, { 1, 0, 0}, { 1, 1, 0}}, // c2
                    {{ 1,-1, 1}, { 1, 0, 0}, { 1,-1, 0}}, // c3
            }
    };
}