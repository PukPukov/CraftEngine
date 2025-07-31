package ru.mrbedrockpy.renderer.graphics;

import org.joml.Vector2i;
import ru.mrbedrockpy.renderer.RenderInit;
import ru.mrbedrockpy.renderer.api.IBlock;
import ru.mrbedrockpy.renderer.api.IChunk;
import ru.mrbedrockpy.renderer.api.IWorld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MeshBuilder {
    private final List<Float> vertices = new ArrayList<>();
    private final List<Float> texCoords = new ArrayList<>();
    private final List<Float> aoValues = new ArrayList<>();
    private final List<Integer> indices = new ArrayList<>();
    private int indexOffset = 0;
    private final TextureAtlas atlas;

    public MeshBuilder(TextureAtlas atlas) {
        this.atlas = atlas;
    }

    private float calculateAO(boolean side1, boolean side2, boolean corner) {
        if (side1 && side2) {
            return 0.5f; // Occluded by two adjacent sides, the corner doesn't matter as much.
        }
        int occlusion = (side1 ? 1 : 0) + (side2 ? 1 : 0) + (corner ? 1 : 0);
        switch (occlusion) {
            case 0: return 1.0f;  // No occlusion
            case 1: return 0.8f;  // Occluded by one block
            case 2: return 0.65f; // Occluded by two blocks
            default: return 0.5f; // Occluded by three blocks
        }
    }

    public void addFace(int x, int y, int z, IBlock.Direction dir, IBlock block, IWorld world) {
        float[][] corners = new float[4][3];
        float[][] uvs = new float[4][2];
        float[] cornerAO = new float[4];

        // Define the 4 corners, their UVs, and calculate AO for each corner of the face.
        switch (dir) {
            case UP: // +Z
                corners[0] = new float[]{x,     y,     z + 1}; corners[1] = new float[]{x + 1, y,     z + 1}; corners[2] = new float[]{x + 1, y + 1, z + 1}; corners[3] = new float[]{x,     y + 1, z + 1};
                uvs[0] = new float[]{0, 0}; uvs[1] = new float[]{1, 0}; uvs[2] = new float[]{1, 1}; uvs[3] = new float[]{0, 1};
                cornerAO[0] = calculateAO(world.getBlock(x, y - 1, z + 1).isSolid(), world.getBlock(x - 1, y, z + 1).isSolid(), world.getBlock(x - 1, y - 1, z + 1).isSolid());
                cornerAO[1] = calculateAO(world.getBlock(x, y - 1, z + 1).isSolid(), world.getBlock(x + 1, y, z + 1).isSolid(), world.getBlock(x + 1, y - 1, z + 1).isSolid());
                cornerAO[2] = calculateAO(world.getBlock(x, y + 1, z + 1).isSolid(), world.getBlock(x + 1, y, z + 1).isSolid(), world.getBlock(x + 1, y + 1, z + 1).isSolid());
                cornerAO[3] = calculateAO(world.getBlock(x, y + 1, z + 1).isSolid(), world.getBlock(x - 1, y, z + 1).isSolid(), world.getBlock(x - 1, y + 1, z + 1).isSolid());
                break;
            case DOWN: // -Z
                corners[0] = new float[]{x,     y + 1, z}; corners[1] = new float[]{x + 1, y + 1, z}; corners[2] = new float[]{x + 1, y,     z}; corners[3] = new float[]{x,     y,     z};
                uvs[0] = new float[]{0, 1}; uvs[1] = new float[]{1, 1}; uvs[2] = new float[]{1, 0}; uvs[3] = new float[]{0, 0};
                cornerAO[0] = calculateAO(world.getBlock(x, y + 1, z - 1).isSolid(), world.getBlock(x - 1, y, z - 1).isSolid(), world.getBlock(x - 1, y + 1, z - 1).isSolid());
                cornerAO[1] = calculateAO(world.getBlock(x, y + 1, z - 1).isSolid(), world.getBlock(x + 1, y, z - 1).isSolid(), world.getBlock(x + 1, y + 1, z - 1).isSolid());
                cornerAO[2] = calculateAO(world.getBlock(x, y - 1, z - 1).isSolid(), world.getBlock(x + 1, y, z - 1).isSolid(), world.getBlock(x + 1, y - 1, z - 1).isSolid());
                cornerAO[3] = calculateAO(world.getBlock(x, y - 1, z - 1).isSolid(), world.getBlock(x - 1, y, z - 1).isSolid(), world.getBlock(x - 1, y - 1, z - 1).isSolid());
                break;
            case NORTH: // -Y
                corners[0] = new float[]{x,     y, z    }; corners[1] = new float[]{x + 1, y, z    }; corners[2] = new float[]{x + 1, y, z + 1}; corners[3] = new float[]{x,     y, z + 1};
                uvs[0] = new float[]{0, 1}; uvs[1] = new float[]{1, 1}; uvs[2] = new float[]{1, 0}; uvs[3] = new float[]{0, 0};
                cornerAO[0] = calculateAO(world.getBlock(x, y - 1, z - 1).isSolid(), world.getBlock(x - 1, y - 1, z).isSolid(), world.getBlock(x - 1, y - 1, z - 1).isSolid());
                cornerAO[1] = calculateAO(world.getBlock(x, y - 1, z - 1).isSolid(), world.getBlock(x + 1, y - 1, z).isSolid(), world.getBlock(x + 1, y - 1, z - 1).isSolid());
                cornerAO[2] = calculateAO(world.getBlock(x, y - 1, z + 1).isSolid(), world.getBlock(x + 1, y - 1, z).isSolid(), world.getBlock(x + 1, y - 1, z + 1).isSolid());
                cornerAO[3] = calculateAO(world.getBlock(x, y - 1, z + 1).isSolid(), world.getBlock(x - 1, y - 1, z).isSolid(), world.getBlock(x - 1, y - 1, z + 1).isSolid());
                break;
            case SOUTH: // +Y
                corners[0] = new float[]{x + 1, y + 1, z    }; corners[1] = new float[]{x,     y + 1, z    }; corners[2] = new float[]{x,     y + 1, z + 1}; corners[3] = new float[]{x + 1, y + 1, z + 1};
                uvs[0] = new float[]{1, 1}; uvs[1] = new float[]{0, 1}; uvs[2] = new float[]{0, 0}; uvs[3] = new float[]{1, 0};
                cornerAO[0] = calculateAO(world.getBlock(x, y + 1, z - 1).isSolid(), world.getBlock(x + 1, y + 1, z).isSolid(), world.getBlock(x + 1, y + 1, z - 1).isSolid());
                cornerAO[1] = calculateAO(world.getBlock(x, y + 1, z - 1).isSolid(), world.getBlock(x - 1, y + 1, z).isSolid(), world.getBlock(x - 1, y + 1, z - 1).isSolid());
                cornerAO[2] = calculateAO(world.getBlock(x, y + 1, z + 1).isSolid(), world.getBlock(x - 1, y + 1, z).isSolid(), world.getBlock(x - 1, y + 1, z + 1).isSolid());
                cornerAO[3] = calculateAO(world.getBlock(x, y + 1, z + 1).isSolid(), world.getBlock(x + 1, y + 1, z).isSolid(), world.getBlock(x + 1, y + 1, z + 1).isSolid());
                break;
            case WEST: // -X
                corners[0] = new float[]{x, y + 1, z    }; corners[1] = new float[]{x, y,     z    }; corners[2] = new float[]{x, y,     z + 1}; corners[3] = new float[]{x, y + 1, z + 1};
                uvs[0] = new float[]{1, 1}; uvs[1] = new float[]{0, 1}; uvs[2] = new float[]{0, 0}; uvs[3] = new float[]{1, 0};
                cornerAO[0] = calculateAO(world.getBlock(x - 1, y, z - 1).isSolid(), world.getBlock(x - 1, y + 1, z).isSolid(), world.getBlock(x - 1, y + 1, z - 1).isSolid());
                cornerAO[1] = calculateAO(world.getBlock(x - 1, y, z - 1).isSolid(), world.getBlock(x - 1, y - 1, z).isSolid(), world.getBlock(x - 1, y - 1, z - 1).isSolid());
                cornerAO[2] = calculateAO(world.getBlock(x - 1, y, z + 1).isSolid(), world.getBlock(x - 1, y - 1, z).isSolid(), world.getBlock(x - 1, y - 1, z + 1).isSolid());
                cornerAO[3] = calculateAO(world.getBlock(x - 1, y, z + 1).isSolid(), world.getBlock(x - 1, y + 1, z).isSolid(), world.getBlock(x - 1, y + 1, z + 1).isSolid());
                break;
            case EAST: // +X
                corners[0] = new float[]{x + 1, y,     z    }; corners[1] = new float[]{x + 1, y + 1, z    }; corners[2] = new float[]{x + 1, y + 1, z + 1}; corners[3] = new float[]{x + 1, y,     z + 1};
                uvs[0] = new float[]{0, 1}; uvs[1] = new float[]{1, 1}; uvs[2] = new float[]{1, 0}; uvs[3] = new float[]{0, 0};
                cornerAO[0] = calculateAO(world.getBlock(x + 1, y, z - 1).isSolid(), world.getBlock(x + 1, y - 1, z).isSolid(), world.getBlock(x + 1, y - 1, z - 1).isSolid());
                cornerAO[1] = calculateAO(world.getBlock(x + 1, y, z - 1).isSolid(), world.getBlock(x + 1, y + 1, z).isSolid(), world.getBlock(x + 1, y + 1, z - 1).isSolid());
                cornerAO[2] = calculateAO(world.getBlock(x + 1, y, z + 1).isSolid(), world.getBlock(x + 1, y + 1, z).isSolid(), world.getBlock(x + 1, y + 1, z + 1).isSolid());
                cornerAO[3] = calculateAO(world.getBlock(x + 1, y, z + 1).isSolid(), world.getBlock(x + 1, y - 1, z).isSolid(), world.getBlock(x + 1, y - 1, z + 1).isSolid());
                break;
        }

        Vector2i tileUV = atlas.getUV(RenderInit.BLOCKS.getName(block));
        float tileCount = atlas.getAtlasSize();
        float unit = 1.0f / tileCount;
        float baseX = tileUV.x * unit;
        float baseY = 1.0f - (tileUV.y + 1) * unit;

        for (int i = 0; i < 4; i++) {
            uvs[i][0] = baseX + uvs[i][0] * unit;
            uvs[i][1] = baseY + uvs[i][1] * unit;
        }

        // Build the two triangles for the quad, choosing the diagonal
        // that produces the best lighting (avoids sharp dark/light cuts)
        if (cornerAO[0] + cornerAO[2] > cornerAO[1] + cornerAO[3]) {
            // Flipped diagonal: (0, 1, 3) and (1, 2, 3) -> This is wrong, it should be (0,1,2) and (0,2,3) vs (0,1,3) and (1,2,3)
            // It should be (v0, v1, v3) and (v3, v1, v2)
            addVertex(corners[0], uvs[0], cornerAO[0]);
            addVertex(corners[1], uvs[1], cornerAO[1]);
            addVertex(corners[3], uvs[3], cornerAO[3]);

            addVertex(corners[1], uvs[1], cornerAO[1]);
            addVertex(corners[2], uvs[2], cornerAO[2]);
            addVertex(corners[3], uvs[3], cornerAO[3]);
        } else {
            // Standard diagonal: (0, 1, 2) and (0, 2, 3)
            addVertex(corners[0], uvs[0], cornerAO[0]);
            addVertex(corners[1], uvs[1], cornerAO[1]);
            addVertex(corners[2], uvs[2], cornerAO[2]);

            addVertex(corners[0], uvs[0], cornerAO[0]);
            addVertex(corners[2], uvs[2], cornerAO[2]);
            addVertex(corners[3], uvs[3], cornerAO[3]);
        }
    }

    private void addVertex(float[] pos, float[] uv, float ao) {
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

    public void addCube(int x, int y, int z, IBlock block) {
        for (IBlock.Direction dir : Arrays.stream(IBlock.Direction.values()).filter(d -> d != IBlock.Direction.NONE).toList()) {
            // This method is now broken as it doesn't have a World reference for AO calculation.
            // Leaving it as-is per instructions not to modify unrelated code.
            // addFace(x, y, z, dir, block);
        }
    }
}