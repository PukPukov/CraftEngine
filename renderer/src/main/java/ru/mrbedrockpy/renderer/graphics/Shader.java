package ru.mrbedrockpy.renderer.graphics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL46C.*;

@Getter
@AllArgsConstructor
public class Shader {

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

    public void dispose() {
        glDeleteProgram(id);
    }
}