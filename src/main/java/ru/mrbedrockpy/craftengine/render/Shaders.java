package ru.mrbedrockpy.craftengine.render;

import ru.mrbedrockpy.craftengine.render.graphics.Shader;
import ru.mrbedrockpy.craftengine.render.util.graphics.ShaderUtil;

public class Shaders {

    public static Shader BLOCK_SHADER = ShaderUtil.load("block.vert", "block.frag");
    public static Shader SKYBOX_SHADER = ShaderUtil.load("skybox.vert", "skybox.frag");
    public static Shader UI_SHADER = ShaderUtil.load("ui.vert", "ui.frag");

    public static void cleanup() {
        if (Shaders.BLOCK_SHADER != null) closeShader(Shaders.BLOCK_SHADER);
        if (Shaders.UI_SHADER != null) closeShader(Shaders.UI_SHADER);
        if (Shaders.SKYBOX_SHADER != null) closeShader(Shaders.SKYBOX_SHADER);
    }

    private static void closeShader(Shader shader) {
        try {
            shader.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
