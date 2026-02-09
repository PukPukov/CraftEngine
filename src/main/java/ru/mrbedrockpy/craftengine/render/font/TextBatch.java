package ru.mrbedrockpy.craftengine.render.font;

import lombok.AllArgsConstructor;
import ru.mrbedrockpy.craftengine.render.Shaders;
import ru.mrbedrockpy.craftengine.render.graphics.MatrixStack;

import static org.lwjgl.opengl.GL11C.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11C.glDrawArrays;
import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL15C.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20C.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20C.glVertexAttrib4f;
import static org.lwjgl.opengl.GL30C.glBindVertexArray;
import static org.lwjgl.opengl.GL30C.glVertexAttribI1i;

@AllArgsConstructor
public class TextBatch {

    private final int vaoId, vboId;
    private final MatrixStack matrices;
    private final int guiAtlas;

    public void maskedQuad(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1, int rgba, float italicSkew) {
        Shaders.UI_SHADER.setUniform1b("useUniformColor", true);
        Shaders.UI_SHADER.setUniform1b("useMask", true);
        Shaders.UI_SHADER.setUniform4f("uniformColor",
                ((rgba>>>16)&0xFF)/255f,
                ((rgba>>> 8)&0xFF)/255f,
                ( rgba      &0xFF)/255f,
                ((rgba>>>24)&0xFF)/255f);
        Shaders.UI_SHADER.setUniformMatrix4f("model", matrices.matrix());
        this.render(this.getVertices(x0, y0, x1, y1, u0, v0, u1, v1));
    }

    // СТИКЕРЫ: цвет из текстуры
    public void texturedQuad(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1) {
        Shaders.UI_SHADER.setUniform1b("useUniformColor", false);
        Shaders.UI_SHADER.setUniform1b("useMask", false);
        Shaders.UI_SHADER.setUniformMatrix4f("model", matrices.matrix());
        render(this.getVertices(x0, y0, x1, y1, u0, v0, u1, v1));
    }

    public void solidQuad(float x0, float y0, float x1, float y1, int rgba) {
        Shaders.UI_SHADER.setUniform1b("useUniformColor", true);
        Shaders.UI_SHADER.setUniform1b("useMask", false);
        Shaders.UI_SHADER.setUniform4f("uniformColor",
                ((rgba>>>16)&0xFF)/255f,
                ((rgba>>> 8)&0xFF)/255f,
                ( rgba      &0xFF)/255f,
                ((rgba>>>24)&0xFF)/255f);
        Shaders.UI_SHADER.setUniformMatrix4f("model", matrices.matrix());
        this.render(this.getSolidVertices(x0, y0, x1, y1));
    }

    private float[] getVertices(float x0, float y0, float x1, float y1, float u0, float v0, float u1, float v1) {
        return new float[] {x0, y0, u0, v0, x1, y0, u1, v0, x1, y1, u1, v1, x0, y0, u0, v0, x1, y1, u1, v1, x0, y1, u0, v1};
    }

    private float[] getSolidVertices(float x0, float y0, float x1, float y1) {
        return new float[] {x0, y0, 0f, 0f, x1, y0, 1f, 0f, x1, y1, 1f, 1f, x0, y0, 0f, 0f, x1, y1, 1f, 1f, x0, y1, 0f, 1f};
    }

    private void render(float[] vertices) {
        glBindVertexArray(this.vaoId);
        glBindBuffer(GL_ARRAY_BUFFER, this.vboId);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
        glDisableVertexAttribArray(3);
        glVertexAttribI1i(3, this.guiAtlas);
        glVertexAttrib4f(2, 1f,1f,1f,1f);
        glDrawArrays(GL_TRIANGLES, 0, 6);
        glBindVertexArray(0);
    }
}