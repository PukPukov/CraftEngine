package ru.mrbedrockpy.renderer.graphics;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;
import ru.mrbedrockpy.craftengine.core.phys.AABB;

public class FrustumCuller {
    private final FrustumIntersection frustum = new FrustumIntersection();

    public void update(Matrix4f projViewMatrix) {
        frustum.set(projViewMatrix);
    }

    public boolean isBoxVisible(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        return frustum.testAab(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public boolean isBoxVisible(AABB aabb){
        return this.isBoxVisible((float) aabb.minX, (float) aabb.minY, (float) aabb.minZ, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ);
    }
}