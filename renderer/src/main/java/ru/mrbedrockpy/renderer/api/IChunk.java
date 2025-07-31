package ru.mrbedrockpy.renderer.api;

import org.joml.Vector2i;
import org.joml.Vector3i;
import ru.mrbedrockpy.renderer.graphics.Mesh;
import ru.mrbedrockpy.renderer.graphics.TextureAtlas;

import java.util.List;

public interface IChunk {
    IBlock getBlock(int x, int y, int z);

    IBlock getBlock(Vector3i pos);
    Vector2i getWorldPosition();
    Vector2i getPosition();
    int WIDTH = 16;
    int HEIGHT = 16;
    void cleanup();
    Mesh getChunkMesh(IWorld world, TextureAtlas atlas);
    void tick();
    boolean setBlock(int x, int y, int z, IBlock block);
    void markDirty();
    void setEntities(List<IEntity> entities);
}
