package ru.mrbedrockpy.renderer.api;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import ru.mrbedrockpy.renderer.world.raycast.BlockRaycastResult;

import java.util.Optional;

public interface IWorld {
    
    IBlock block(int x, int y, int z);
    IBlock block(Vector3i pos);
    @Nullable IChunk chunk(int x, int z);
    @Nullable IChunk chunk(Vector2i pos);
    @Nullable IChunk chunkByBlockPosition(int x, int z);
    int size();
    IChunk[][] chunks();
    BlockRaycastResult raycast(Vector3f originF, Vector3f directionF, float maxDistanceF);
    
}