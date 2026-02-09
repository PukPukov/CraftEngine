package ru.mrbedrockpy.craftengine.render.world;

import org.joml.Matrix4f;
import ru.mrbedrockpy.craftengine.render.Shaders;
import ru.mrbedrockpy.craftengine.render.util.graphics.TextureUtil;

import static org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays;
import static org.lwjgl.opengl.GL46C.*;

public class SkyboxRenderer implements AutoCloseable {

    private final int cubemapId;
    private final int vao;

    public SkyboxRenderer(String atlas3x2Path) {
        cubemapId = TextureUtil.loadCubemapFromAtlas3x2(atlas3x2Path);

        glEnable(GL_TEXTURE_CUBE_MAP_SEAMLESS);
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubemapId);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glGenerateMipmap(GL_TEXTURE_CUBE_MAP);
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);

        vao = glGenVertexArrays();
        glBindVertexArray(vao);
        glBindVertexArray(0);
    }

    public void render(Matrix4f projection, Matrix4f view) {
        glDepthFunc(GL_LEQUAL);
        glDepthMask(false);

        boolean cull = glIsEnabled(GL_CULL_FACE);
        if (cull) glDisable(GL_CULL_FACE);

        Matrix4f viewNoPos = new Matrix4f(view).m30(0).m31(0).m32(0);

        Matrix4f invProj = new Matrix4f(projection).invert();
        Matrix4f invView = new Matrix4f(viewNoPos).invert();

        Shaders.SKYBOX_SHADER.use();

        Shaders.SKYBOX_SHADER.setUniformMatrix4f("uInvProj", invProj);
        Shaders.SKYBOX_SHADER.setUniformMatrix4f("uInvView", invView);
        Shaders.SKYBOX_SHADER.setUniform1i("skybox", 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubemapId);

        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glBindVertexArray(0);

        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        Shaders.SKYBOX_SHADER.unbind();

        if (cull) glEnable(GL_CULL_FACE);
        glDepthMask(true);
        glDepthFunc(GL_LESS);
    }

    @Override
    public void close() throws Exception {
        // VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vao);

        // Cubemap
        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        glDeleteTextures(cubemapId);
    }
}