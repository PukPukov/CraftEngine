package ru.mrbedrockpy.craftengine.graphics;

import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.world.block.Block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MeshBuilder {
    private final List<Float> vertices = new ArrayList<>();
    private final List<Float> texCoords = new ArrayList<>();
    private final List<Integer> indices = new ArrayList<>();
    private int indexOffset = 0;
    private final TextureAtlas atlas;

    public MeshBuilder(TextureAtlas atlas) {
        this.atlas = atlas;
    }

    public void addFace(int x, int y, int z, Block.Direction dir, Block block) {
        float[] faceVerts = FaceData.getVertices(dir, x, y, z);
        float[] faceUVs   = FaceData.getUVs(block, dir, atlas);

        int vertCount = faceVerts.length / 3;
        if (faceUVs.length / 2 != vertCount) {
            throw new IllegalStateException("UV count does not match vertex count");
        }
        for (int i = 0; i < vertCount; i++) {
            vertices.add(faceVerts[i * 3]);
            vertices.add(faceVerts[i * 3 + 1]);
            vertices.add(faceVerts[i * 3 + 2]);

            texCoords.add(faceUVs[i * 2]);
            texCoords.add(faceUVs[i * 2 + 1]);
        }

        indices.add(indexOffset);
        indices.add(indexOffset + 1);
        indices.add(indexOffset + 2);

        indices.add(indexOffset + 3);
        indices.add(indexOffset + 4);
        indices.add(indexOffset + 5);

        indexOffset += vertCount;
    }

    public Mesh.MeshData buildData() {
        float[] vertArray = new float[vertices.size()];
        float[] uvArray = new float[texCoords.size()];

        for (int i = 0; i < vertArray.length; i++) vertArray[i] = vertices.get(i);
        for (int i = 0; i < uvArray.length; i++) uvArray[i] = texCoords.get(i);

        return new Mesh.MeshData(vertArray, uvArray);
    }
}