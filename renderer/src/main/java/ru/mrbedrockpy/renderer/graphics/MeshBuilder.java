package ru.mrbedrockpy.renderer.graphics;

import org.joml.Vector2i;
import ru.mrbedrockpy.renderer.RenderInit;
import ru.mrbedrockpy.renderer.api.IBlock;
import ru.mrbedrockpy.renderer.api.IWorld;

import java.util.ArrayList;
import java.util.List;

public class MeshBuilder {
    private final List<Float> vertices = new ArrayList<>();
    private final List<Float> texCoords = new ArrayList<>();
    private final List<Float> aoValues = new ArrayList<>();
    private final TextureAtlas atlas;

    public MeshBuilder(TextureAtlas atlas) {
        this.atlas = atlas;
    }

    private float calculateAO(boolean side1, boolean side2, boolean corner) {
        if (side1 && side2) {
            return 0.5f; // Occluded by two adjacent sides, the corner doesn't matter as much.
        }
        int occlusion = (side1 ? 1 : 0) + (side2 ? 1 : 0) + (corner ? 1 : 0);
        return switch (occlusion) {
            case 0 -> 1.0f;  // No occlusion
            case 1 -> 0.8f;  // Occluded by one block
            case 2 -> 0.65f; // Occluded by two blocks
            default -> 0.5f; // Occluded by three blocks
        };
    }

    public void addFace(int x, int y, int z, IBlock.Direction dir, IBlock block, IWorld world) {
        FaceData data = FaceData.valueOf(dir.name());

        float[][] corners = new float[4][3];
        for (int i = 0; i < 4; i++) {
            corners[i][0] = x + data.corners[i][0];
            corners[i][1] = y + data.corners[i][1];
            corners[i][2] = z + data.corners[i][2];
        }

        float[][] uvs = {
            {0, 0}, {1, 0}, {1, 1}, {0, 1}
        };

        float[] cornerAO = new float[4];
        for (int i = 0; i < 4; i++) {
            int[] o1 = data.aoOffsets[i][0];
            int[] o2 = data.aoOffsets[i][1];
            int[] o3 = data.aoOffsets[i][2];

            cornerAO[i] = calculateAO(
                    world.block(x + o1[0], y + o1[1], z + o1[2]).solid(),
                    world.block(x + o2[0], y + o2[1], z + o2[2]).solid(),
                    world.block(x + o3[0], y + o3[1], z + o3[2]).solid()
            );
        }

        Vector2i tileUV = atlas.uv(RenderInit.BLOCKS.name(block));
        float tileCount = atlas.atlasSize();
        float unit = 1.0f / tileCount;
        float baseX = tileUV.x * unit;
        float baseY = 1.0f - (tileUV.y + 1) * unit;

        for (int i = 0; i < 4; i++) {
            uvs[i][0] = baseX + uvs[i][0] * unit;
            uvs[i][1] = baseY + uvs[i][1] * unit;
        }

        // Это не OpenGL индексы — просто порядок вершин, передаваемых в v()
        int[][] indices;
        if (cornerAO[0] + cornerAO[2] > cornerAO[1] + cornerAO[3]) {
            indices = new int[][]{
                    {0, 1, 3},
                    {1, 2, 3}
            };
        } else {
            indices = new int[][]{
                    {0, 1, 2},
                    {0, 2, 3}
            };
        }

        for (int[] tri : indices) {
            for (int idx : tri) {
                v(corners[idx], uvs[idx], cornerAO[idx]);
            }
        }
    }

    // Поскольку метод private и он в классе связянным с мешем можно сократить addVertex до v
    private void v(float[] pos, float[] uv, float ao) {
        vertices.add(pos[0]);
        vertices.add(pos[1]);
        vertices.add(pos[2]);
        texCoords.add(uv[0]);
        texCoords.add(uv[1]);
        aoValues.add(ao);
    }

    public Mesh.MeshData buildData() {
        float[] vertArray = new float[vertices.size()];
        float[] uvArray = new float[texCoords.size()];
        float[] aoArray = new float[aoValues.size()];

        for (int i = 0; i < vertArray.length; i++) vertArray[i] = vertices.get(i);
        for (int i = 0; i < uvArray.length; i++) uvArray[i] = texCoords.get(i);
        for (int i = 0; i < aoArray.length; i++) aoArray[i] = aoValues.get(i);

        return new Mesh.MeshData(vertArray, uvArray, aoArray);
    }

    public enum FaceData {
        UP(
            new int[][]{
                {0, 0, 1}, {1, 0, 1}, {1, 1, 1}, {0, 1, 1}
            },
            new int[][][]{
                {{0, -1, 1}, {-1, 0, 1}, {-1, -1, 1}},
                {{0, -1, 1}, { 1, 0, 1}, { 1, -1, 1}},
                {{0,  1, 1}, { 1, 0, 1}, { 1,  1, 1}},
                {{0,  1, 1}, {-1, 0, 1}, {-1,  1, 1}}
            }
        ),
        DOWN(
            new int[][]{
                {0, 1, 0}, {1, 1, 0}, {1, 0, 0}, {0, 0, 0}
            },
            new int[][][]{
                {{0, 1, -1}, {-1, 0, -1}, {-1, 1, -1}},
                {{0, 1, -1}, { 1, 0, -1}, { 1, 1, -1}},
                {{0, -1, -1}, {1, 0, -1}, {1, -1, -1}},
                {{0, -1, -1}, {-1, 0, -1}, {-1, -1, -1}}
            }
        ),
        NORTH(
            new int[][]{
                {0, 0, 0}, {1, 0, 0}, {1, 0, 1}, {0, 0, 1}
            },
            new int[][][]{
                {{0, -1, -1}, {-1, -1, 0}, {-1, -1, -1}},
                {{0, -1, -1}, { 1, -1, 0}, { 1, -1, -1}},
                {{0, -1, 1}, { 1, -1, 0}, { 1, -1, 1}},
                {{0, -1, 1}, {-1, -1, 0}, {-1, -1, 1}}
            }
        ),
        SOUTH(
            new int[][]{
                {1, 1, 0}, {0, 1, 0}, {0, 1, 1}, {1, 1, 1}
            },
            new int[][][]{
                {{0, 1, -1}, {1, 1, 0}, {1, 1, -1}},
                {{0, 1, -1}, {-1, 1, 0}, {-1, 1, -1}},
                {{0, 1, 1}, {-1, 1, 0}, {-1, 1, 1}},
                {{0, 1, 1}, {1, 1, 0}, {1, 1, 1}}
            }
        ),
        WEST(
            new int[][]{
                {0, 1, 0}, {0, 0, 0}, {0, 0, 1}, {0, 1, 1}
            },
            new int[][][]{
                {{-1, 1, -1}, {-1, 0, 0}, {-1, 1, 0}},
                {{-1, -1, -1}, {-1, 0, 0}, {-1, -1, 0}},
                {{-1, -1, 1}, {-1, 0, 0}, {-1, -1, 0}},
                {{-1, 1, 1}, {-1, 0, 0}, {-1, 1, 0}}
            }
        ),
        EAST(
                new int[][]{
                {1, 0, 0}, {1, 1, 0}, {1, 1, 1}, {1, 0, 1}
            },
            new int[][][]{
                {{1, -1, -1}, {1, 0, 0}, {1, -1, 0}},
                {{1, 1, -1}, {1, 0, 0}, {1, 1, 0}},
                {{1, 1, 1}, {1, 0, 0}, {1, 1, 0}},
                {{1, -1, 1}, {1, 0, 0}, {1, -1, 0}}
            }
        );

        public final int[][] corners;
        public final int[][][] aoOffsets;

        FaceData(int[][] corners, int[][][] aoOffsets) {
            this.corners = corners;
            this.aoOffsets = aoOffsets;
        }
    }
}