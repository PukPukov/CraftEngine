package ru.mrbedrockpy.craftengine.graphics;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryUtil;
import ru.mrbedrockpy.craftengine.window.Camera;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL46C.*;

public class Mesh {
    private final int vaoId;
    private final int vboId;
    private final int uvboId;
    private final int eboId;
    private final int indexCount;
    private final float[] positions;
    private final float[] uvs;
    private final int[] indices;

    public Mesh(float[] vertices, float[] texCoords, int[] indices) {
        this(vertices, texCoords, indices, true);
    }

    public Mesh(float[] vertices, float[] texCoords, int[] indices, boolean flip) {
        indexCount = indices.length;
        positions = vertices;
        uvs = texCoords;
        this.indices = indices;

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        uvboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, uvboId);
        glBufferData(GL_ARRAY_BUFFER, texCoords, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(1);

        eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.length);
        indexBuffer.put(indices);
        if(flip) {
            indexBuffer.flip();
        }
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public Mesh(FloatBuffer vertices, FloatBuffer texCoords, IntBuffer indices, boolean flip) {
        indexCount = indices.limit();
        positions = vertices.array();
        uvs = texCoords.array();
        this.indices = indices.array();

        if (flip) {
            vertices.flip();
            texCoords.flip();
            indices.flip();
        }

        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        uvboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, uvboId);
        glBufferData(GL_ARRAY_BUFFER, texCoords, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(1);

        eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void render() {
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);
    }

    public static Mesh mergeMeshes(List<Mesh> meshes, boolean bound) {
        List<MeshData> dataList = new ArrayList<>();
        for (Mesh mesh : meshes) {
            dataList.add(mesh.getMeshData());
            mesh.cleanup();
        }
        return mergeMeshes(dataList);
    }

    public static Mesh mergeMeshes(List<MeshData> meshDataList) {
        int totalPositions = 0;
        int totalUVs = 0;
        int totalIndices = 0;

        for (MeshData data : meshDataList) {
            totalPositions += data.positions().length;
            totalUVs += data.uvs().length;
            totalIndices += data.indices().length;
        }

        float[] combinedPositions = new float[totalPositions];
        float[] combinedUVs = new float[totalUVs];
        int[] combinedIndices = new int[totalIndices];

        int posOffset = 0;
        int uvOffset = 0;
        int indexOffset = 0;
        int vertexOffset = 0;

        for (MeshData data : meshDataList) {
            float[] positions = data.positions();
            float[] uvs = data.uvs();
            int[] indices = data.indices();

            System.arraycopy(positions, 0, combinedPositions, posOffset, positions.length);
            System.arraycopy(uvs, 0, combinedUVs, uvOffset, uvs.length);

            for (int i = 0; i < indices.length; i++) {
                combinedIndices[indexOffset + i] = indices[i] + vertexOffset;
            }

            posOffset += positions.length;
            uvOffset += uvs.length;
            indexOffset += indices.length;
            vertexOffset += positions.length / 3;
        }

        return new Mesh(combinedPositions, combinedUVs, combinedIndices);
    }



    public void cleanup() {
        glDeleteVertexArrays(vaoId);
        glDeleteBuffers(vboId);
        glDeleteBuffers(uvboId);
        glDeleteBuffers(eboId);
    }

    public MeshData getMeshData() {
        return new MeshData(positions, uvs, indices);
    }

    public record MeshData(float[] positions, float[] uvs, int[] indices){}

}