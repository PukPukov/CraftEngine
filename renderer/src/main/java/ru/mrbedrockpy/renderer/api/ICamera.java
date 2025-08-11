package ru.mrbedrockpy.renderer.api;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public interface ICamera {
    Matrix4f getViewMatrix();
    Matrix4f getProjectionMatrix();
    Vector3f getPosition();
    Vector3f getFront();
    Vector3f getFlatFront();
}