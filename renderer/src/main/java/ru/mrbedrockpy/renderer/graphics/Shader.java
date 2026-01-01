package ru.mrbedrockpy.renderer.graphics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL46C.*;

@Getter
@AllArgsConstructor
public class Shader implements AutoCloseable {

    private final int id;

    public void use() {
        glUseProgram(id);
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void setUniformMatrix4f(String name, Matrix4f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(16);
            matrix.get(buffer);
            int location = glGetUniformLocation(id, name);
            if (location != -1) glUniformMatrix4fv(location, false, buffer);
            else System.err.println("Warning: uniform '" + name + "' not found in shader program.");
        }
    }

    public void setUniform4f(String name, float r, float g, float b, float a) {
        int location = glGetUniformLocation(id, name);
        if (location != -1)
            glUniform4f(location, r, g, b, a);
        else
            System.err.println("Uniform not found: " + name);
    }

    public void setUniform4fColor(String name, Color color) {
        setUniform4f(name, color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
    }

    public void setUniform1b(String name, boolean value) {
        int location = glGetUniformLocation(id, name);
        if (location != -1)
            glUniform1i(location, value ? 1 : 0); // booleans are set as integers in OpenGL
        else
            System.err.println("Uniform not found: " + name);
    }

    public void setUniform1i(String name, int i) {
        int location = glGetUniformLocation(id, name);
        if (location != -1)
            glUniform1i(location, i);
        else
            System.err.println("Uniform not found: " + name);
    }

    @Override
    public void close() throws Exception {
        glDeleteProgram(id);
    }
}