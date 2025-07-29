package ru.mrbedrockpy.craftengine.graphics;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import ru.mrbedrockpy.craftengine.window.Camera;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL46C.*;

public class Mesh {
    
    private final int vaoId;
    private final int vboId;
    private final int uvboId;
    private final int aoboId;
    private final int eboId;
    private final int vertexCount;
    
    public Mesh(float[] vertices, float[] texCoords, float[] aoValues) {
        vertexCount = vertices.length / 3;
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
        
        aoboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, aoboId);
        glBufferData(GL_ARRAY_BUFFER, aoValues, GL_STATIC_DRAW);
        glVertexAttribPointer(2, 1, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(2);
        
        eboId = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
        
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }
    
    public void bind() {
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
    }
    
    public void unbind() {
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);
    }
    
    public void render() {
        glBindVertexArray(vaoId);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
//        glDrawElements(GL_TRIANGLES, indexCount, GL_UNSIGNED_INT, 0);
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glBindVertexArray(0);
    }

//    public MeshData getMeshData() {
//        glBindVertexArray(vaoId);
//        FloatBuffer positionBuffer = MemoryUtil.memAllocFloat(indexCount * 3);
//        FloatBuffer uvBuffer = MemoryUtil.memAllocFloat(indexCount * 2);
//
//        glGetBufferSubData(GL_ARRAY_BUFFER, 0, positionBuffer);
//        glGetBufferSubData(GL_ARRAY_BUFFER, indexCount * 3 * Float.BYTES, uvBuffer);
//
//        float[] positions = new float[positionBuffer.remaining()];
//        float[] uvs = new float[uvBuffer.remaining()];
//
//        positionBuffer.get(positions);
//        uvBuffer.get(uvs);
//
//        MemoryUtil.memFree(positionBuffer);
//        MemoryUtil.memFree(uvBuffer);
//
//        return new MeshData(positions, uvs);
//    }
    
    public static Mesh mergeMeshes(List<MeshData> meshDataList) {
        List<Float> combinedPositions = new ArrayList<>();
        List<Float> combinedUVs = new ArrayList<>();
        List<Float> combinedAOs = new ArrayList<>();
        
        int vertexOffset = 0;
        
        for (MeshData data : meshDataList) {
            float[] positions = data.positions;
            float[] uvs = data.uvs;
            float[] aos = data.aoValues;
            
            for (int i = 0; i < positions.length; i += 3) {
                combinedPositions.add(positions[i]);
                combinedPositions.add(positions[i + 1]);
                combinedPositions.add(positions[i + 2]);
            }
            
            for (int i = 0; i < uvs.length; i += 2) {
                combinedUVs.add(uvs[i]);
                combinedUVs.add(uvs[i + 1]);
            }
            
            for (float ao : aos) {
                combinedAOs.add(ao);
            }
            
            vertexOffset += positions.length / 3;
        }
        
        
        float[] finalPositions = new float[combinedPositions.size()];
        float[] finalUVs = new float[combinedUVs.size()];
        float[] finalAOs = new float[combinedAOs.size()];
        
        for (int i = 0; i < finalPositions.length; i++) finalPositions[i] = combinedPositions.get(i);
        for (int i = 0; i < finalUVs.length; i++) finalUVs[i] = combinedUVs.get(i);
        for (int i = 0; i < finalAOs.length; i++) finalAOs[i] = combinedAOs.get(i);
        
        return new Mesh(finalPositions, finalUVs, finalAOs);
    }
    
    public void cleanup() {
        glDeleteVertexArrays(vaoId);
        glDeleteBuffers(vboId);
        glDeleteBuffers(uvboId);
        glDeleteBuffers(aoboId);
        glDeleteBuffers(eboId);
    }
    
    public record MeshData(float[] positions, float[] uvs, float[] aoValues){}
}