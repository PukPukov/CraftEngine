package ru.mrbedrockpy.renderer.graphics;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL46C.*;
public class Mesh {
    private int vaoId;
    private int vboId;
    private int uvboId;
    private int aoboId;
    private int vertexCount;

    public Mesh() {
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public Mesh vertices(float[] vertices){
        if(isArrayEmpty(vertices)) return this;
        vertexCount = vertices.length / 3;

        use();
        vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);
        unbind();
        return this;
    }

    public Mesh uvs(float[] uvs){
        if(isArrayEmpty(uvs)) return this;
        use();
        uvboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, uvboId);
        glBufferData(GL_ARRAY_BUFFER, uvs, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(1);
        unbind();
        return this;
    }

    public Mesh aos(float[] aos){
        if(isArrayEmpty(aos)) return this;
        use();
        aoboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, aoboId);
        glBufferData(GL_ARRAY_BUFFER, aos, GL_STATIC_DRAW);
        glVertexAttribPointer(2, 1, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(2);
        unbind();
        return this;
    }

    public Mesh data(Data data){
        return this.vertices(data.vertices).uvs(data.uvs).aos(data.aos);
    }

    public void render() {
        use();
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        unbind();
    }

    public void use() {
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

    public void cleanup() {
        glDeleteVertexArrays(vaoId);
        if (vboId  != 0) glDeleteBuffers(vboId);
        if (uvboId != 0) glDeleteBuffers(uvboId);
        if (aoboId != 0) glDeleteBuffers(aoboId);
    }

    private boolean isArrayEmpty(float[] arr){
        return arr.length == 0;
    }

    public record Data(float[] vertices, float[] uvs, float[] aos){}
}