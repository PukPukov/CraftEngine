package ru.mrbedrockpy.craftengine.render.graphics;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Consumer;

import static org.lwjgl.opengl.GL46C.*;

public class Mesh implements AutoCloseable {
    @Getter
    private int vaoId;
    @Getter
    private int vboId;
    private int uvboId;
    private int nboId;
    @Getter
    private int vertexCount;

    public Mesh() {
        vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public static Mesh fromData(Data meshData) {
        Mesh mesh = new Mesh();
        mesh.data(meshData);
        return mesh;
    }

    public Mesh vertices(float[] vertices) {
        if(isArrayEmpty(vertices)) throw new IllegalArgumentException("vertices array is empty");
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
        if(isArrayEmpty(uvs)) throw new IllegalArgumentException("uvs array is empty");
        use();
        uvboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, uvboId);
        glBufferData(GL_ARRAY_BUFFER, uvs, GL_STATIC_DRAW);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(1);
        unbind();
        return this;
    }

    public Mesh normals(float[] normals) {
        use();
        nboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, nboId);
        glBufferData(GL_ARRAY_BUFFER, normals, GL_STATIC_DRAW);
        glVertexAttribPointer(4, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(4);
        unbind();
        return this;
    }

    public Mesh data(Data data){
        return this.vertices(data.vertices).uvs(data.uvs).normals(data.normals);
    }

    public void render() {
        use();
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
        unbind();
    }

    public void render(Consumer<Mesh> drawCall) {
        use();
        try {
            drawCall.accept(this);
        } finally {
            unbind();
        }
    }

    public void use() {
        glBindVertexArray(vaoId);
    }

    public void unbind() {
        glBindVertexArray(0);
    }

    private boolean isArrayEmpty(float[] arr){
        return arr == null || arr.length == 0;
    }

    @Override
    public void close() throws Exception {
        glDeleteVertexArrays(vaoId);
        if (vboId  != 0) glDeleteBuffers(vboId);
        if (uvboId != 0) glDeleteBuffers(uvboId);
    }

    @Getter
    @AllArgsConstructor
    public static class Data {
        private final float[] vertices, uvs, normals;
    }
}
