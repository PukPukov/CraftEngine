package ru.mrbedrockpy.renderer.api;

import org.joml.Vector2i;
import org.joml.Vector3i;
import ru.mrbedrockpy.renderer.graphics.Mesh;
import ru.mrbedrockpy.renderer.graphics.TextureAtlas;

import java.util.List;

public interface IChunk {
    IBlock block(int x, int y, int z);

    IBlock block(Vector3i pos);
    Vector2i worldPosition();
    Vector2i getPosition();
    int WIDTH = 16;
    int HEIGHT = 100;
    void cleanup();
    Mesh chunkMesh(IWorld world, TextureAtlas atlas);
    void tick();
    boolean setBlock(int x, int y, int z, IBlock block);
    void markDirty();
    void setEntities(List<IEntity> entities);
}