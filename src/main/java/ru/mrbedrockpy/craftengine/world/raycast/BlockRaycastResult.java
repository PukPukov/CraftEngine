package ru.mrbedrockpy.craftengine.world.raycast;

import lombok.AllArgsConstructor;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.world.block.Block;

@AllArgsConstructor
public class BlockRaycastResult {
    public final int x, y, z;
    public final Block block;
    public final Block.Direction direction;
    public Vector3i getPosition() {
        return new Vector3i(x, y, z);
    }
}