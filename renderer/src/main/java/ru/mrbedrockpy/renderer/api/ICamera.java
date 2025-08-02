package ru.mrbedrockpy.renderer.api;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public interface ICamera {
    Matrix4f viewMatrix();
    Matrix4f projectionMatrix();
    Vector3f position();
    Vector3f front();
    Vector3f flatFront();
}