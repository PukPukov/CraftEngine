package ru.mrbedrockpy.renderer.graphics.tex;

import lombok.Getter;
import ru.mrbedrockpy.craftengine.core.util.id.RL;

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

    // Какая то баганая вайб хуйня
    public TextureRegion findRegion(RL key) {
        RL norm = RL.of(key.namespace(), key.path().endsWith(".png") ? key.path().substring(0, key.path().length() - ".png".length()) : key.path());
        for (int i = 0; i < atlases.size(); i++) {
            TextureRegion r = atlases.get(i).region(i, norm);
            if (r != null) return r;
        }
        return null;
    }

    public void uploadToShader(int program, String uniformArrayBaseName) {
        if (bindless) {
            for (int i = 0; i < atlases.size(); i++) {
                int loc = glGetUniformLocation(program, uniformArrayBaseName + "[" + i + "]");
                if (loc < 0) continue; // или throw, если хочешь жёстко
                long h = atlases.get(i).texture().handle(); // см. пункт 2 про resident
                glProgramUniformHandleui64ARB(program, loc, h);
            }
        } else {
            if (legacyUnits == null || legacyUnits.length < atlases.size()) {
                legacyUnits = new int[atlases.size()];
                for (int i = 0; i < legacyUnits.length; i++) legacyUnits[i] = i;
            }

            for (int i = 0; i < atlases.size(); i++) {
                glActiveTexture(GL_TEXTURE0 + i);
                glBindTexture(GL_TEXTURE_2D, atlases.get(i).texture().getId());
            }

            for (int i = 0; i < atlases.size(); i++) {
                int loc = glGetUniformLocation(program, uniformArrayBaseName + "[" + i + "]");
                if (loc < 0) continue;
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