package ru.mrbedrockpy.renderer.graphics.tex;

import lombok.Getter;

import static org.lwjgl.opengl.ARBBindlessTexture.*;
import static org.lwjgl.opengl.GL46C.*;
import java.util.*;

public final class AtlasManager implements AutoCloseable {
    private final List<Atlas> atlases = new ArrayList<>();
    @Getter
    private final boolean bindless;
    private int[] legacyUnits;

    public AtlasManager() {
        this.bindless = org.lwjgl.opengl.GL.getCapabilities().GL_ARB_bindless_texture;
    }

    public int register(Atlas atlas) {
        atlases.add(atlas);
        return atlases.size() - 1;
    }

    public Atlas get(int index) { return atlases.get(index); }
    public int size() { return atlases.size(); }

    public TextureRegion findRegion(String key) {
        for (int i = 0; i < atlases.size(); i++) {
            TextureRegion r = atlases.get(i).region(i, key);
            if (r != null) return r;
        }
        return null;
    }

    public void uploadToShader(int program, String uniformArrayBaseName) {
        if (bindless) {
            int baseLoc = glGetUniformLocation(program, uniformArrayBaseName + "[0]");
            for (int i = 0; i < atlases.size(); i++) {
                long h = atlases.get(i).texture().handle();
                glProgramUniformHandleui64ARB(program, baseLoc + i, h);
            }
        } else {
            if (legacyUnits == null || legacyUnits.length < atlases.size()) {
                legacyUnits = new int[atlases.size()];
                for (int i = 0; i < legacyUnits.length; i++) legacyUnits[i] = i;
            }
            for (int i = 0; i < atlases.size(); i++) {
                glActiveTexture(GL_TEXTURE0 + i);
                glBindTexture(GL_TEXTURE_2D, atlases.get(i).texture().id());
            }
            for (int i = 0; i < atlases.size(); i++) {
                int loc = glGetUniformLocation(program, uniformArrayBaseName + "[" + i + "]");
                glProgramUniform1i(program, loc, i);
            }
            glActiveTexture(GL_TEXTURE0);
        }
    }

    @Override public void close() {
        for (Atlas a : atlases) a.texture().close();
        atlases.clear();
    }
}