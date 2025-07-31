package ru.mrbedrockpy.renderer.api;

import org.joml.Vector3f;
import org.joml.Vector3i;
import ru.mrbedrockpy.renderer.world.raycast.BlockRaycastResult;

public interface IWorld {
    IBlock getBlock(int x, int y, int z);
    IBlock getBlock(Vector3i pos);
    IChunk getChunkByChunkPos(int x, int z);
    IChunk getChunkByBlockPos(int x, int z);
    int getWorldSize();
    IChunk[][] getChunks();
    BlockRaycastResult raycast(Vector3f originF, Vector3f directionF, float maxDistanceF);
}
