package ru.mrbedrockpy.craftengine.render.util.graphics;

import org.lwjgl.system.MemoryStack;
import ru.mrbedrockpy.craftengine.render.graphics.Shader;
import ru.mrbedrockpy.craftengine.render.util.FileLoader;

import java.io.File;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL46C.*;

public class ShaderUtil {

    private static final Map<String, Shader> cache = new HashMap<>();
    private static final String SHADER_PATH = "assets" + File.separator + "craftengine" + File.separator + "shaders" + File.separator;

    public static Shader load(String vertexPath, String fragmentPath) {
        String key = vertexPath + "|" + fragmentPath;
        return cache.computeIfAbsent(key, k -> new Shader(ShaderUtil.linkProgram(
                ShaderUtil.compileShader(vertexPath, GL_VERTEX_SHADER),
                ShaderUtil.compileShader(fragmentPath, GL_FRAGMENT_SHADER)
        )));
    }

    public static int compileShader(String resourcePath, int shaderType) {
        String shaderCode = "";
        try {
            shaderCode = FileLoader.loadString(SHADER_PATH + resourcePath);
        } catch (Exception e){
            e.printStackTrace();
        }
        if (shaderCode.isEmpty()) {
            throw new RuntimeException("Failed to load shader code: " + shaderCode);
        }

        int shaderID = glCreateShader(shaderType);
        if (shaderID == 0) throw new RuntimeException("Shader creation failed for: " + resourcePath);

        glShaderSource(shaderID, shaderCode);
        glCompileShader(shaderID);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer success = stack.mallocInt(1);
            glGetShaderiv(shaderID, GL_COMPILE_STATUS, success);
            if (success.get(0) == GL_FALSE) {
                String log = glGetShaderInfoLog(shaderID);
                throw new RuntimeException("Shader compilation failed: " + resourcePath + "\n" + log);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return shaderID;
    }

    public static int linkProgram(int... shaders) {
        int programID = glCreateProgram();
        for (int s : shaders) glAttachShader(programID, s);
        glLinkProgram(programID);
        checkProgramStatus(programID, GL_LINK_STATUS);
        glValidateProgram(programID);
        checkProgramStatus(programID, GL_VALIDATE_STATUS);
        for (int s : shaders) glDeleteShader(s);
        return programID;
    }

    private static void checkProgramStatus(int programID, int statusType) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer success = stack.mallocInt(1);
            glGetProgramiv(programID, statusType, success);
            if (success.get(0) == GL_FALSE) {
                String log = glGetProgramInfoLog(programID);
                String statusName = (statusType == GL_LINK_STATUS) ? "Linking" : "Validation";
                throw new RuntimeException("Program " + statusName + " failed:\n" + log);
            }
        }
    }
}
