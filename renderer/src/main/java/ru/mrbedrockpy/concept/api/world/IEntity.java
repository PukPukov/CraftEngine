package ru.mrbedrockpy.concept.api.world;

import org.joml.Vector2d;
import org.joml.Vector3d;
import ru.mrbedrockpy.renderer.graphics.Mesh;

public interface IEntity {

    Vector3d getPosition();

    Vector2d getRotation();

    Mesh getMesh();

    void setMesh(Mesh mesh);

}
