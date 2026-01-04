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

        // Готовим углы юнит-грани (или возьми из getCornersForModel(modelName))
        final float[][][] baseFaceCorners = getCornersForModel("cube_all");

        // 1) Грузим модель для блока (пример: все блоки кубические)
        final String modelName = "cube_all";
        final Model model = RenderInit.RESOURCE_MANAGER.getModelLoader().loadOrNull(modelName);

        // 2) Нормализация UV: размеры атласа и flipV под твой рендер
        final float atlasW = 32 * 16;     // или 1, если уже [0..1];
        final float atlasH = 32 * 16;    // или 1;
        final boolean flipV = false;


        for (int z = 0; z < SIZE; z++) {
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    short id = chunk.getBlocks()[x][y][z];
                    if (id == 0) continue;

                    for (Block.Direction d : Block.Direction.getValues()) {
                        Vector3i n = d.offset();
                        if (isSolidWorld(chunk, x + n.x, y + n.y, z + n.z)) continue;

                        float[][] corners = baseFaceCorners[d.ordinal()];
                        RL block = RenderInit.BLOCKS.getRL(RenderInit.BLOCKS.get(id));
                        float[] uv4 = atlas.getNormalizedUvs(RL.of(block.namespace(), "block/" + block.path()));

                        emitFace(x, y, z, d, corners, uv4, chunk);
                    }
                }
            }
        }

        return new Mesh.Data(
                toFloatArray(vertices),
                toFloatArray(texCoords)
        );
    }

    private void v(float[] pos, float[] uv) {
        vertices.add(pos[0]); vertices.add(pos[1]); vertices.add(pos[2]);
        texCoords.add(uv[0]); texCoords.add(uv[1]);
    }

    private void emitFace(int bx, int by, int bz,
                          Block.Direction d,
                          float[][] corners,
                          float[] uv4,
                          Chunk chunk) {
        final int[][] tri = {{0, 1, 2}, {0, 2, 3}};
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
                float[] pos = new float[]{ p[0] + bx + chunk.getWorldPosition().x, p[2] + bz, p[1] + by + chunk.getWorldPosition().y };
                v(pos, uvByCorner[c]);
            }
        }
    }


    private static float[] toFloatArray(List<Float> src) {
        float[] a = new float[src.size()];
        for (int i = 0; i < src.size(); i++) a[i] = src.get(i);
        return a;
    }

    private static final int[][] FACE_CORNERS_ZUP = new int[6][4];
    static {
        // DOWN=z0, UP=z1, NORTH=y-, SOUTH=y+, WEST=x-, EAST=x+
        FACE_CORNERS_ZUP[Block.Direction.NORTH.ordinal()] = new int[]{0, 4, 5, 1}; // y=y0
        FACE_CORNERS_ZUP[Block.Direction.SOUTH.ordinal()] = new int[]{3, 2, 6, 7}; // y=y1
        FACE_CORNERS_ZUP[Block.Direction.DOWN.ordinal()]  = new int[]{0, 1, 2, 3}; // z=z0
        FACE_CORNERS_ZUP[Block.Direction.UP.ordinal()]    = new int[]{5, 4, 7, 6}; // z=z1
        FACE_CORNERS_ZUP[Block.Direction.WEST.ordinal()]  = new int[]{4, 0, 3, 7}; // x=x0
        FACE_CORNERS_ZUP[Block.Direction.EAST.ordinal()]  = new int[]{1, 5, 6, 2}; // x=x1
    }

    private float[][][] getCornersForModel(String modelName) {
        // Возвращаем по 4 угла на грань (6 граней)
        float[][][] out = new float[Block.Direction.values().length][4][3];

        Model model = RenderInit.RESOURCE_MANAGER.getModelLoader().loadOrNull(modelName);
        if (model == null || model.getBones().isEmpty()) return out;

        Vector3f min = new Vector3f(Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY);

        // Собираем AABB модели с учётом костей (Z-up)
        for (Bone bone : model.getBones()) {
            Matrix4f m = boneZupMatrix(bone); // rotationZ * rotateY * rotateX * translate

            for (Cuboid c : bone.getCuboids()) {
                // 8 локальных углов кубоида
                Vector3f[] v = getCuboidCornersLocal(c);

                // Трансформируем и расширяем AABB
                for (int i = 0; i < 8; i++) {
                    Vector3f tp = new Vector3f();
                    m.transformPosition(v[i], tp); // НЕ мутируем исходный массив
                    min.min(tp);
                    max.max(tp);
                }
            }
        }

        if (!(min.x < max.x && min.y < max.y && min.z < max.z)) return out;

        // 8 углов AABB в Z-up (z0 слой и z1 слой)
        float x0 = min.x, y0 = min.y, z0 = min.z;
        float x1 = max.x, y1 = max.y, z1 = max.z;

        float[][] corners8 = new float[][]{
                {x0, y0, z0}, {x1, y0, z0}, {x1, y1, z0}, {x0, y1, z0}, // 0..3 (z0)
                {x0, y0, z1}, {x1, y0, z1}, {x1, y1, z1}, {x0, y1, z1}  // 4..7 (z1)
        };

        // Разложим 8 углов по 6 граням (по 4 угла на грань) в корректном порядке
        for (Block.Direction d : Block.Direction.getValues()) {
            int[] idx = FACE_CORNERS_ZUP[d.ordinal()];
            for (int i = 0; i < 4; i++) {
                float[] src = corners8[idx[i]];
                out[d.ordinal()][i][0] = src[0];
                out[d.ordinal()][i][1] = src[1];
                out[d.ordinal()][i][2] = src[2];
            }
        }

        return out;
    }

    private static Vector3f[] getCuboidCornersLocal(Cuboid cube) {
        Vector3f pos = cube.getPosition();
        Vector3f size = cube.getSize();

        float x0 = pos.x, y0 = pos.y, z0 = pos.z;
        float x1 = x0 + size.x, y1 = y0 + size.y, z1 = z0 + size.z;

        return new Vector3f[]{
                new Vector3f(x0, y0, z0),
                new Vector3f(x1, y0, z0),
                new Vector3f(x1, y1, z0),
                new Vector3f(x0, y1, z0),
                new Vector3f(x0, y0, z1),
                new Vector3f(x1, y0, z1),
                new Vector3f(x1, y1, z1),
                new Vector3f(x0, y1, z1)
        };
    }

    private static Matrix4f boneZupMatrix(Bone b) {
        Vector3f rot = b.getRotation();  // градусы (предположим)
        Vector3f pos = b.getPosition();  // смещение кости

        float rx = (float) Math.toRadians(rot.x);
        float ry = (float) Math.toRadians(rot.y);
        float rz = (float) Math.toRadians(rot.z);

        return new Matrix4f()
                .rotationZ(rz)
                .rotateY(ry)
                .rotateX(rx)
                .translate(pos.x, pos.y, pos.z);
    }

    private boolean isSolidWorld(Chunk chunk, int lx, int ly, int lz) {
        Vector2i base = chunk.getWorldPosition();
        int wx = base.x + lx;
        int wy = base.y + ly;
        int wz = lz;
        return reader.isSolid(wx, wy, wz);
    }
}