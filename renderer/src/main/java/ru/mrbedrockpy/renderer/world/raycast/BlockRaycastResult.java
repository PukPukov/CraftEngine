package ru.mrbedrockpy.renderer.world.raycast;

import lombok.AllArgsConstructor;
import org.joml.Vector3i;
import ru.mrbedrockpy.renderer.api.IBlock;

@AllArgsConstructor
public class BlockRaycastResult {
    public final int x, y, z;
    public final IBlock block;
    public final IBlock.Direction direction;
    public Vector3i getPosition() {
        return new Vector3i(x, y, z);
    }
}