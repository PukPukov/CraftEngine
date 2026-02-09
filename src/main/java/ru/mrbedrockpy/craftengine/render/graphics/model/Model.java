package ru.mrbedrockpy.craftengine.render.graphics.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Model {

    private final List<Bone> bones = new ArrayList<>();

    public Model addBone(Bone bone) {
        bones.add(bone);
        return this;
    }

    @Nullable
    public Bone findBone(String name) {
        for (var b : bones) if (b.getName().equals(name)) return b;
        return null;
    }
}