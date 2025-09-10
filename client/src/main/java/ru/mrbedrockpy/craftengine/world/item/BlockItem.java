package ru.mrbedrockpy.craftengine.world.item;

import lombok.Getter;
import org.joml.Vector3f;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.world.World;
import ru.mrbedrockpy.craftengine.world.block.Block;
import ru.mrbedrockpy.craftengine.world.entity.ClientPlayerEntity;
import ru.mrbedrockpy.craftengine.world.inventory.PlayerInventory;
import ru.mrbedrockpy.renderer.world.raycast.BlockRaycastResult;

public class BlockItem extends Item{
    /**
     * Конструктор для создания предмета.
     * в будущем могут быть приватные поля, поэтому без lombok.
     *
     * @param displayName  Отображаемое имя предмета
     * @param maxStackSize Максимальный размер стека предметов
     */
    @Getter
    private final Block block;
    public BlockItem(String displayName, int maxStackSize, Block block) {
        super(displayName, maxStackSize);
        this.block = block;
    }

    @Override
    public void use(ClientPlayerEntity player) {
        Vector3f rayOrigin = player.getCamera().getPosition();
        Vector3f rayDirection = player.getCamera().getFront();
        World world = player.getWorld();
        PlayerInventory inventory = player.getInventory();
        if(inventory.getSelectedStack().item() instanceof BlockItem) {
            BlockRaycastResult blockRaycastResult = world.raycast(rayOrigin, rayDirection, 4.5f);
            if (blockRaycastResult != null && world.canPlaceBlockAt(blockRaycastResult.position().add(blockRaycastResult.direction.offset()))) {
                Vector3i blockPos = blockRaycastResult.position().add(blockRaycastResult.direction.offset());
                world.setBlock(blockPos.x, blockPos.y, blockPos.z, block);
            }
        }
    }
}
