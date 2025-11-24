package ru.mrbedrockpy.craftengine.core.world.item;

import lombok.Getter;
import org.joml.Vector3f;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.core.world.World;
import ru.mrbedrockpy.craftengine.core.world.block.Block;
import ru.mrbedrockpy.craftengine.core.world.entity.PlayerEntity;
import ru.mrbedrockpy.craftengine.core.world.inventory.PlayerInventory;
import ru.mrbedrockpy.craftengine.core.world.raycast.BlockRaycastResult;

public class BlockItem extends Item {

    @Getter
    private final Block block;
    public BlockItem(String displayName, int maxStackSize, Block block) {
        super(displayName, maxStackSize);
        this.block = block;
    }

    @Override
    public void use(PlayerEntity player) {
        Vector3f rayOrigin = new Vector3f(player.getPosition()).add(0, 0, player.getEyeOffset());
        Vector3f rayDirection = player.getFront();
        World world = player.getWorld();
        BlockRaycastResult blockRaycastResult = world.raycast(rayOrigin, rayDirection, 4.5f);
        if (blockRaycastResult != null && world.canPlaceBlockAt(blockRaycastResult.position().add(blockRaycastResult.direction.offset()))) {
            Vector3i blockPos = blockRaycastResult.position().add(blockRaycastResult.direction.offset());
            world.setBlock(blockPos.x, blockPos.y, blockPos.z, block);
        }
    }
}
