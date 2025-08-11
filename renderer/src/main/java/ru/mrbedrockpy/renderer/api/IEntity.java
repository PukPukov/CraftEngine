package ru.mrbedrockpy.renderer.api;

import org.joml.Vector2i;
import org.joml.Vector3f;
import ru.mrbedrockpy.renderer.phys.AABB;

public interface IEntity {
    
    Vector2i getChunkPosition();
    Vector3f getTickPosition();
    void tick();
    AABB getBoundingBox();
    
}