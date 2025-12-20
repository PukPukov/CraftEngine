package ru.mrbedrockpy.concept.api.world;

import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector3d;

public interface ICamera {

    Vector3d getPosition();

    Vector2d getRotation();

    float getFov();

    void setFov(float fov);

    float getZNear();

    float getZFar();

    Matrix4f getProjectionMatrix();

    Matrix4f getViewMatrix();

}
