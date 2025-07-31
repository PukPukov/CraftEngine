package ru.mrbedrockpy.craftengine.world.block;


import ru.mrbedrockpy.renderer.api.IBlock;
import ru.mrbedrockpy.renderer.phys.AABB;

public class Block implements IBlock {
    private final boolean solid;

    public Block(boolean solid) {
        this.solid = solid;
    }

    @Override
    public boolean isSolid() {
        return solid;
    }

    @Override
    public AABB getAABB(int x, int y, int z) {
        return new AABB(x, y, z, x + 1, y + 1, z + 1);
    }
}