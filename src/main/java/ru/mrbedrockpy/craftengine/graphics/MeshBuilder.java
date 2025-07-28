package ru.mrbedrockpy.craftengine.graphics;

import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.world.block.Block;

import java.util.*;

public class MeshBuilder {
    private final List<Float> vertices = new ArrayList<>();
    private final List<Float> texCoords = new ArrayList<>();
    private final List<Integer> indices = new ArrayList<>();
    private int indexOffset = 0;
    private final TextureAtlas atlas;

    public MeshBuilder(TextureAtlas atlas) {
        this.atlas = atlas;
    }

    public void addFaces(int x, int y, int z, List<Block.Direction> dirs, Block block) {
        float[] buffer = new float[24];

        for (Block.Direction dir : dirs) {
            float[] verts = FaceData.getVertices(dir, x, y, z);
            float[] uvs   = FaceData.getUVs(block, dir, atlas);

            for (int i = 0; i < 4; i++) {
                int vi = i * 3;
                int ui = i * 2;
                int bi = i * 6;

                buffer[bi    ] = verts[vi];
                buffer[bi + 1] = verts[vi + 1];
                buffer[bi + 2] = verts[vi + 2];
                buffer[bi + 3] = uvs[ui];
                buffer[bi + 4] = uvs[ui + 1];
                buffer[bi + 5] = 0f;
            }

            for (int i = 0; i < 4; i++) {
                int bi = i * 6;
                vertices.add(buffer[bi]);
                vertices.add(buffer[bi + 1]);
                vertices.add(buffer[bi + 2]);

                texCoords.add(buffer[bi + 3]);
                texCoords.add(buffer[bi + 4]);
            }

            indices.add(indexOffset);
            indices.add(indexOffset + 1);
            indices.add(indexOffset + 2);

            indices.add(indexOffset);
            indices.add(indexOffset + 2);
            indices.add(indexOffset + 3);

            indexOffset += 4;
        }
    }


    public Mesh.MeshData buildData() {
        float[] vertArray = new float[vertices.size()];
        float[] uvArray = new float[texCoords.size()];
        int[] indicesArray = new int[this.indices.size()];

        for (int i = 0; i < vertArray.length; i++) vertArray[i] = vertices.get(i);
        for (int i = 0; i < uvArray.length; i++) uvArray[i] = texCoords.get(i);
        for (int i = 0; i < indicesArray.length; i++) indicesArray[i] = indices.get(i);

        return new Mesh.MeshData(vertArray, uvArray, indicesArray);
    }

}