package ru.mrbedrockpy.craftengine.render.graphics.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Bone {

    private String name;

    private Vector3f position = new Vector3f();
    private Vector3f rotation = new Vector3f();

    private final List<Cuboid> cuboids = new ArrayList<>();

    public Bone(String name) {
        this.name = name;
    }

    public Bone setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        return this;
    }

    public Bone setRotation(float x, float y, float z) {
        this.rotation.set(x, y, z);
        return this;
    }

    public Bone addCuboid(Cuboid cuboid) {
        this.cuboids.add(cuboid);
        return this;
    }
}