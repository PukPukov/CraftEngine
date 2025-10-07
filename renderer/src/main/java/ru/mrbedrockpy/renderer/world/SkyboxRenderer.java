package ru.mrbedrockpy.renderer.world;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import ru.mrbedrockpy.renderer.graphics.Mesh;
import ru.mrbedrockpy.renderer.graphics.Shader;
import ru.mrbedrockpy.renderer.graphics.Texture;
import ru.mrbedrockpy.renderer.util.ImageUtil;
import ru.mrbedrockpy.renderer.util.graphics.ShaderUtil;
import ru.mrbedrockpy.renderer.util.graphics.TextureUtil;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL46C.*;


public class SkyboxRenderer {

    private final int vao;
    private final int vbo;
    private final Shader shader;
    private final int cubemapId;

    // Куб из 36 вершин
    private static final float[] SKYBOX_VERTS = {
            -1,  1, -1,  -1, -1, -1,   1, -1, -1,
             1, -1, -1,   1,  1, -1,  -1,  1, -1,
            -1, -1,  1,  -1, -1, -1,  -1,  1, -1,
            -1,  1, -1,  -1,  1,  1,  -1, -1,  1,
             1, -1, -1,   1, -1,  1,   1,  1,  1,
             1,  1,  1,   1,  1, -1,   1, -1, -1,
            -1, -1,  1,  -1,  1,  1,   1,  1,  1,
             1,  1,  1,   1, -1,  1,  -1, -1,  1,
            -1,  1, -1,   1,  1, -1,   1,  1,  1,
             1,  1,  1,  -1,  1,  1,  -1,  1, -1,
            -1, -1, -1,  -1, -1,  1,   1, -1,  1,
             1, -1,  1,   1, -1, -1,  -1, -1, -1
    };

    public SkyboxRenderer(String[] faces) {
        shader = ShaderUtil.load("skybox_vert.glsl", "skybox_frag.glsl");
        cubemapId = TextureUtil.loadCubemap(faces);

        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);

        FloatBuffer buf = BufferUtils.createFloatBuffer(SKYBOX_VERTS.length);
        buf.put(SKYBOX_VERTS).flip();

        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, buf, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0);
        glBindVertexArray(0);
    }

    /**
     * Рендер skybox
     */
    public void render(Matrix4f projection, Matrix4f view) {
        glDepthFunc(GL_LEQUAL);
        glDepthMask(false);

        boolean cull = glIsEnabled(GL_CULL_FACE);
        if (cull) glDisable(GL_CULL_FACE);

        shader.use();

        Matrix4f viewNoPos = new Matrix4f(view).m30(0).m31(0).m32(0);
        shader.setUniformMatrix4f("projection", projection);
        shader.setUniformMatrix4f("view", viewNoPos);
        shader.setUniform1i("skybox", 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubemapId);

        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glBindVertexArray(0);

        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        shader.unbind();

        if (cull) glEnable(GL_CULL_FACE);
        glDepthMask(true);
        glDepthFunc(GL_LESS);
    }

}