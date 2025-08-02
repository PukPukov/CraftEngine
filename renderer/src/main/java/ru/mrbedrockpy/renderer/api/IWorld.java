package ru.mrbedrockpy.renderer.api;

import org.joml.Vector3f;
import org.joml.Vector3i;
import ru.mrbedrockpy.renderer.world.raycast.BlockRaycastResult;

public interface IWorld {
    
    IBlock block(int x, int y, int z);
    IBlock block(Vector3i pos);
    IChunk chunk(int x, int z);
    IChunk chunkByBlockPosition(int x, int z);
    int size();
    IChunk[][] chunks();
    BlockRaycastResult raycast(Vector3f originF, Vector3f directionF, float maxDistanceF);
    
}