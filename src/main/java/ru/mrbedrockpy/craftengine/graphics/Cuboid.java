package ru.mrbedrockpy.craftengine.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.window.Camera;

import static org.lwjgl.opengl.GL46C.*;


public class Cuboid {

    private  final OutlineMesh outlineMesh;
    private final Vector3f position;
    private final Vector3f size;
    private Texture texture;

    public Cuboid(Vector3f position, Vector3f size) {
        this.position = new Vector3f(position);
        this.size = new Vector3f(size);
        float[] texCoords = generateTexCoords();
//        this.mesh = new Mesh(generateVertices(), texCoords);
        this.texture = Texture.load("block.png");
        this.outlineMesh = new OutlineMesh(generateVertices(), texCoords);
    }

    public Matrix4f getModelMatrix() {
        return new Matrix4f()
                .translate(position)
                .scale(size);
    }

    public void render(Matrix4f view, Matrix4f projection) {
        texture.use();
//        mesh.render(getModelMatrix(), view, projection);
    }

    public void renderOutline(Matrix4f view, Matrix4f projection) {
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        glLineWidth(2.0f);
        glDepthMask(false);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        outlineMesh.render(getModelMatrix(), view, projection);

         glDepthFunc(GL_LESS);
        glDepthMask(true);
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
    }

    public void cleanup() {
        outlineMesh.cleanup();
    }


    public float[] generateTexCoords() {
        float[] uvFace = {
                0f, 0f, 1f, 0f, 1f, 1f,
                1f, 1f, 0f, 1f, 0f, 0f
        };

        float[] texCoords = new float[6 * 6 * 2];
        for (int i = 0; i < 6; i++) {
            System.arraycopy(uvFace, 0, texCoords, i * 12, 12);
        }
        return texCoords;
    }
    public float[] generateVertices() {
        Vector3f[] v = {
                new Vector3f(0f, 0f, 0f),
                new Vector3f( 1f, 0f, 0f),
                new Vector3f( 1f,  1f, 0f),
                new Vector3f(0f,  1f, 0f),
                new Vector3f(0f, 0f,  1f),
                new Vector3f( 1f, 0f,  1f),
                new Vector3f( 1f,  1f,  1f),
                new Vector3f(0f,  1f,  1f)
        };

        int[] indices = {
                0, 1, 2, 2, 3, 0,
                5, 4, 7, 7, 6, 5,
                4, 0, 3, 3, 7, 4,
                1, 5, 6, 6, 2, 1,
                3, 2, 6, 6, 7, 3,
                4, 5, 1, 1, 0, 4
        };

        float[] vertices = new float[indices.length * 3];
        for (int i = 0; i < indices.length; i++) {
            Vector3f vert = v[indices[i]];
            vertices[i * 3] = vert.x;
            vertices[i * 3 + 1] = vert.y;
            vertices[i * 3 + 2] = vert.z;
        }

        return vertices;
    }


}