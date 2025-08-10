package ru.mrbedrockpy.renderer.api;

import org.joml.Vector2i;
import org.joml.Vector3f;
import ru.mrbedrockpy.renderer.phys.AABB;

public interface IEntity {
    
    Vector2i chunkPosition();
    Vector3f tickPosition();
    void tick();
    AABB boundingBox();
    
}