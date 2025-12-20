package ru.mrbedrockpy.concept.api.world;

import org.joml.Vector3i;
import ru.mrbedrockpy.renderer.graphics.Mesh;

public interface IChunk {

    Mesh getMesh();

    void setMesh(Mesh mesh);

    int getChunkX();

    int getChunkY();

    int getChunkZ();

    Vector3i getChunkCoords();

    int getChunkSize();

}
