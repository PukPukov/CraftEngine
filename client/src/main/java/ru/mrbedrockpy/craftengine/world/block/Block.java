package ru.mrbedrockpy.craftengine.world.block;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.joml.Vector3i;
import ru.mrbedrockpy.renderer.api.IBlock;
import ru.mrbedrockpy.renderer.phys.AABB;


@Getter
public class Block implements IBlock {
    
    private final String id;
    private final boolean solid;

    public Block(String id, boolean solid){
        this.id = id;
        this.solid = solid;
    }

    public AABB getAABB(int x, int y, int z) {
        return new AABB(x, y, z, x + 1, y + 1, z + 1);
    }



    @Override
    public String toString() {
        return "Block: {id:" + id + "}";
    }
}