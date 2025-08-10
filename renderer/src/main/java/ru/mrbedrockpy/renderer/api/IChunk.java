package ru.mrbedrockpy.renderer.api;

import org.joml.Vector2i;
import org.joml.Vector3i;
import ru.mrbedrockpy.renderer.graphics.Mesh;
import ru.mrbedrockpy.renderer.graphics.TextureAtlas;

import java.util.List;

public interface IChunk {

    int WIDTH = 16;
    int HEIGHT = 100;

    IBlock getBlock(int x, int y, int z);

    IBlock getBlock(Vector3i pos);
    Vector2i getWorldPosition();
    Vector2i getPosition();
    void cleanup();
    Mesh chunkMesh(IWorld world, TextureAtlas atlas);
    void tick();
    boolean setBlock(int x, int y, int z, IBlock block);
    void markDirty();
    void setEntities(List<IEntity> entities);
}