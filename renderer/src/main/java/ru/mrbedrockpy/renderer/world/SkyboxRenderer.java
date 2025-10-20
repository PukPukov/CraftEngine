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

    private final Shader shader;
    private final int cubemapId;
    private final int vao;

    public SkyboxRenderer(String atlas3x2Path) {
        shader = ShaderUtil.load("skybox_vert.glsl", "skybox_frag.glsl");
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

        shader.use();

        Matrix4f viewNoPos = new Matrix4f(view).m30(0).m31(0).m32(0);

        Matrix4f invProj = new Matrix4f(projection).invert();
        Matrix4f invView = new Matrix4f(viewNoPos).invert();

        shader.setUniformMatrix4f("uInvProj", invProj);
        shader.setUniformMatrix4f("uInvView", invView);
        shader.setUniform1i("skybox", 0);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubemapId);

        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glBindVertexArray(0);

        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
        shader.unbind();

        if (cull) glEnable(GL_CULL_FACE);
        glDepthMask(true);
        glDepthFunc(GL_LESS);
    }
}