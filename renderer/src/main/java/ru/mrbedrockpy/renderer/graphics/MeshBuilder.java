package ru.mrbedrockpy.renderer.graphics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ru.mrbedrockpy.renderer.RenderInit;
import ru.mrbedrockpy.renderer.api.IBlock;
import ru.mrbedrockpy.renderer.api.IWorld;

import java.util.*;

public class MeshBuilder {
    private final List<Float> vertices = new ArrayList<>();
    private final List<Float> texCoords = new ArrayList<>();
    private final List<Float> aoValues  = new ArrayList<>();
    private final TextureAtlas atlas;

    private static final int DIRS = IBlock.Direction.values().length - 1;

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
        float[][] c = modelCorners[d];

        float[][] corners = new float[4][3];
        for (int i = 0; i < 4; i++) {
            corners[i][0] = x + c[i][0];
            corners[i][1] = y + c[i][1];
            corners[i][2] = z + c[i][2];
        }

        float[] nuv = atlas.normalizedUv(RenderInit.BLOCKS.getName(block));
        float[][] uvs = new float[][]{
            { nuv[0], nuv[1] },
            { nuv[2], nuv[3] },
            { nuv[4], nuv[5] },
            { nuv[6], nuv[7] }
        };

        int[] nrm     = normalOf(dir);   // (nx,ny,nz) ∈ {-1,0,1}
        int[][] basis = basisUV(dir);    // basis[0]=u, basis[1]=v (каждый ∈ {-1,0,1}^3)

        float[] cornerAO = new float[4];
        for (int i = 0; i < 4; i++) {
            int[] s = cornerSigns(i);       // su, sv ∈ {-1,+1}
            int su = s[0], sv = s[1];

            int o1x = nrm[0] + sv * basis[1][0]; // n + sv*v
            int o1y = nrm[1] + sv * basis[1][1];
            int o1z = nrm[2] + sv * basis[1][2];

            int o2x = nrm[0] + su * basis[0][0]; // n + su*u
            int o2y = nrm[1] + su * basis[0][1];
            int o2z = nrm[2] + su * basis[0][2];

            int o3x = nrm[0] + su * basis[0][0] + sv * basis[1][0]; // n + su*u + sv*v
            int o3y = nrm[1] + su * basis[0][1] + sv * basis[1][1];
            int o3z = nrm[2] + su * basis[0][2] + sv * basis[1][2];

            cornerAO[i] = calculateAO(
                world.block(x + o1x, y + o1y, z + o1z).isSolid(),
                world.block(x + o2x, y + o2y, z + o2z).isSolid(),
                world.block(x + o3x, y + o3y, z + o3z).isSolid()
            );
        }

        int[] idx = (cornerAO[0] + cornerAO[2] > cornerAO[1] + cornerAO[3])
                ? new int[]{0, 1, 3, 1, 2, 3}
                : new int[]{0, 1, 2, 0, 2, 3};

        for (int i : idx) v(corners[i], uvs[i], cornerAO[i]);
    }

    private float[][][] getCornersForModel(String modelName) {
        float[][][] out = new float[DIRS][4][3];

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
            out[d.ordinal()] = parsed;
        }

        return out;
    }

    private void v(float[] pos, float[] uv, float ao) {
        vertices.add(pos[0]); vertices.add(pos[1]); vertices.add(pos[2]);
        texCoords.add(uv[0]); texCoords.add(uv[1]);
        aoValues.add(ao);
    }

    public Mesh.Data buildData() {
        float[] vArr = new float[vertices.size()];
        float[] tArr = new float[texCoords.size()];
        float[] aArr = new float[aoValues.size()];
        for (int i = 0; i < vArr.length; i++) vArr[i] = vertices.get(i);
        for (int i = 0; i < tArr.length; i++) tArr[i] = texCoords.get(i);
        for (int i = 0; i < aArr.length; i++) aArr[i] = aoValues.get(i);
        return new Mesh.Data(vArr, tArr, aArr);
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

    private static int[] normalOf(IBlock.Direction d) {
        return switch (d) {
            case UP    -> new int[]{ 0, 0, 1};
            case DOWN  -> new int[]{ 0, 0,-1};
            case NORTH -> new int[]{ 0,-1, 0};
            case SOUTH -> new int[]{ 0, 1, 0};
            case WEST  -> new int[]{-1, 0, 0};
            case EAST  -> new int[]{ 1, 0, 0};
            default    -> new int[]{ 0, 0, 0};
        };
    }

    private static int[][] basisUV(IBlock.Direction d) {
        return switch (d) {
            case UP, DOWN      -> new int[][]{ {1,0,0}, {0,1,0} };
            case NORTH, SOUTH  -> new int[][]{ {1,0,0}, {0,0,1} };
            case WEST, EAST    -> new int[][]{ {0,1,0}, {0,0,1} };
            default            -> new int[][]{ {1,0,0}, {0,1,0} };
        };
    }

    private static int[] cornerSigns(int cornerIndex) {
        return switch (cornerIndex) {
            case 0 -> new int[]{-1, -1};
            case 1 -> new int[]{+1, -1};
            case 2 -> new int[]{+1, +1};
            case 3 -> new int[]{-1, +1};
            default -> new int[]{-1, -1};
        };
    }
}