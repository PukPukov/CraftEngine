package ru.mrbedrockpy.renderer.graphics;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.joml.Vector2i;
import org.joml.Vector3i;
import ru.mrbedrockpy.renderer.RenderInit;
import ru.mrbedrockpy.renderer.api.IBlock;
import ru.mrbedrockpy.renderer.api.IWorld;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MeshBuilder {
    private final List<Float> vertices = new ArrayList<>();
    private final List<Float> texCoords = new ArrayList<>();
    private final List<Float> aoValues  = new ArrayList<>();
    private final TextureAtlas atlas;

    private static final int DIRS = IBlock.Direction.values().length - 1;

    private static final int[][][][] AO_OFFSETS = {
        // UP
        {
            { { 0,-1, 1}, {-1, 0, 1}, {-1,-1, 1} },
            { { 0,-1, 1}, { 1, 0, 1}, { 1,-1, 1} },
            { { 0, 1, 1}, { 1, 0, 1}, { 1, 1, 1} },
            { { 0, 1, 1}, {-1, 0, 1}, {-1, 1, 1} }
        },
        // DOWN
        {
            { { 0, 1,-1}, {-1, 0,-1}, {-1, 1,-1} },
            { { 0, 1,-1}, { 1, 0,-1}, { 1, 1,-1} },
            { { 0,-1,-1}, { 1, 0,-1}, { 1,-1,-1} },
            { { 0,-1,-1}, {-1, 0,-1}, {-1,-1,-1} }
        },
        // NORTH
        {
            { { 0,-1,-1}, {-1,-1, 0}, {-1,-1,-1} },
            { { 0,-1,-1}, { 1,-1, 0}, { 1,-1,-1} },
            { { 0,-1, 1}, { 1,-1, 0}, { 1,-1, 1} },
            { { 0,-1, 1}, {-1,-1, 0}, {-1,-1, 1} }
        },
        // SOUTH
        {
            { { 0, 1,-1}, { 1, 1, 0}, { 1, 1,-1} },
            { { 0, 1,-1}, {-1, 1, 0}, {-1, 1,-1} },
            { { 0, 1, 1}, {-1, 1, 0}, {-1, 1, 1} },
            { { 0, 1, 1}, { 1, 1, 0}, { 1, 1, 1} }
        },
        // WEST
        {
            { {-1, 0,-1}, {-1, 1, 0}, {-1, 1,-1} },
            { {-1, 0,-1}, {-1,-1, 0}, {-1,-1,-1} },
            { {-1, 0, 1}, {-1,-1, 0}, {-1,-1, 1} },
            { {-1, 0, 1}, {-1, 1, 0}, {-1, 1, 1} }
        },
        // EAST
        {
            { { 1, 0,-1}, { 1,-1, 0}, { 1,-1,-1} },
            { { 1, 0,-1}, { 1, 1, 0}, { 1, 1,-1} },
            { { 1, 0, 1}, { 1, 1, 0}, { 1, 1, 1} },
            { { 1, 0, 1}, { 1,-1, 0}, { 1,-1, 1} }
        }
    };

    // Кэш: модельное имя -> [dir][4][3]

    public MeshBuilder(TextureAtlas atlas) {
        this.atlas = atlas;
    }

    private float calculateAO(boolean side1, boolean side2, boolean corner) {
        if (side1 && side2) return 0.5f;
        int occlusion = (side1 ? 1 : 0) + (side2 ? 1 : 0) + (corner ? 1 : 0);
        return switch (occlusion) { case 0 -> 1.0f; case 1 -> 0.8f; case 2 -> 0.65f; default -> 0.5f; };
    }

    public void addFace(int x, int y, int z, IBlock.Direction dir, IBlock block, IWorld world) {
        String modelName = modelNameFor(block);
        float[][][] modelCorners = getCornersForModel(modelName);

        int d = dir.ordinal();
        float[][]   c  = modelCorners[d];
        int[][][] ao = AO_OFFSETS[d];

        float[][] corners = new float[4][3];
        for (int i = 0; i < 4; i++) {
            corners[i][0] = x + c[i][0];
            corners[i][1] = y + c[i][1];
            corners[i][2] = z + c[i][2];
        }

        float[] normalizedUvs = atlas.normalizedUv(RenderInit.BLOCKS.getName(block));
        float[][] uvs = new float[][]{
            { normalizedUvs[0], normalizedUvs[1] },
            { normalizedUvs[2], normalizedUvs[3] },
            { normalizedUvs[4], normalizedUvs[5] },
            { normalizedUvs[6], normalizedUvs[7] }
        };

        float[] cornerAO = new float[4];
        for (int i = 0; i < 4; i++) {
            int[] o1 = ao[i][0], o2 = ao[i][1], o3 = ao[i][2];
            cornerAO[i] = calculateAO(
                world.block(x + o1[0], y + o1[1], z + o1[2]).isSolid(),
                world.block(x + o2[0], y + o2[1], z + o2[2]).isSolid(),
                world.block(x + o3[0], y + o3[1], z + o3[2]).isSolid()
            );
        }

        int[] idx = (cornerAO[0] + cornerAO[2] > cornerAO[1] + cornerAO[3])
                ? new int[]{0,1,3, 1,2,3}
                : new int[]{0,1,2, 0,2,3};

        for (int i : idx) v(corners[i], uvs[i], cornerAO[i]);
    }

    private float[][][] getCornersForModel(String modelName) {
        float[][][] out = new float[DIRS][4][3];

//        for (IBlock.Direction d : IBlock.Direction.values()) {
//            out[d.ordinal()] = defaultFaceCorners(d);
//        }

        JsonObject model = RenderInit.RESOURCE_MANAGER.getModel("cube_all");
        if (model == null) return out;

        for (IBlock.Direction d : IBlock.Direction.values()) {
            if(d == IBlock.Direction.NONE) continue;
            String key = d.name();
            JsonObject face = null;

            if (model.has(key) && model.get(key).isJsonObject()) {
                face = model.getAsJsonObject(key);
            } else {
                String low = key.toLowerCase();
                if (model.has(low) && model.get(low).isJsonObject()) {
                    face = model.getAsJsonObject(low);
                }
            }

            if (face == null) continue;
            if (!face.has("vertices") || !face.get("vertices").isJsonArray()) continue;

            float[][] parsed = parseVerts(face.getAsJsonArray("vertices"));
            if (parsed != null) out[d.ordinal()] = parsed;
        }

        return out;
    }

    private void v(float[] pos, float[] uv, float ao) {
        vertices.add(pos[0]); vertices.add(pos[1]); vertices.add(pos[2]);
        texCoords.add(uv[0]); texCoords.add(uv[1]);
        aoValues.add(ao);
    }

    public Mesh.MeshData buildData() {
        float[] vArr = new float[vertices.size()];
        float[] tArr = new float[texCoords.size()];
        float[] aArr = new float[aoValues.size()];
        for (int i = 0; i < vArr.length; i++) vArr[i] = vertices.get(i);
        for (int i = 0; i < tArr.length; i++) tArr[i] = texCoords.get(i);
        for (int i = 0; i < aArr.length; i++) aArr[i] = aoValues.get(i);
        return new Mesh.MeshData(vArr, tArr, aArr);
    }

    private static String modelNameFor(IBlock block) {
        return RenderInit.BLOCKS.getName(block);
    }

    private static float[][] parseVerts(JsonArray arr) {
        float[][] v = new float[4][3];
        for (int i = 0; i < Math.min(4, arr.size()); i++) {
            JsonArray p = arr.get(i).getAsJsonArray();
            v[i][0] = p.get(0).getAsFloat();
            v[i][1] = p.get(1).getAsFloat();
            v[i][2] = p.get(2).getAsFloat();
        }
        return v;
    }
    private static int[][] defaultFaceCorners(IBlock.Direction d) {
        return switch (d) {
            case UP -> new int[][]{{0, 0, 1}, {1, 0, 1}, {1, 1, 1}, {0, 1, 1}};
            case DOWN -> new int[][]{{0, 1, 0}, {1, 1, 0}, {1, 0, 0}, {0, 0, 0}};
            case NORTH -> new int[][]{{0, 0, 0}, {1, 0, 0}, {1, 0, 1}, {0, 0, 1}};
            case SOUTH -> new int[][]{{1, 1, 0}, {0, 1, 0}, {0, 1, 1}, {1, 1, 1}};
            case WEST -> new int[][]{{0, 1, 0}, {0, 0, 0}, {0, 0, 1}, {0, 1, 1}};
            case EAST -> new int[][]{{1, 0, 0}, {1, 1, 0}, {1, 1, 1}, {1, 0, 1}};
            case NONE -> null;
        };
    }
}