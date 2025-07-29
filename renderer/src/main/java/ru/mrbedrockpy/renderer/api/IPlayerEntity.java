package ru.mrbedrockpy.renderer.api;

import org.joml.Vector2i;
import org.joml.Vector3f;

public interface IPlayerEntity {
    float getEyeOffset();
    Vector2i getChunkPosition();
}
