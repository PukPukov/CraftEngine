package ru.mrbedrockpy.craftengine.core.world.block;


import lombok.Getter;
import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.core.phys.AABB;
import ru.mrbedrockpy.craftengine.core.registry.Registries;

import java.util.List;


@Getter
public class Block {
    
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

    public enum Direction {
        UP(0, 0, 1),
        DOWN(0, 0, -1),
        NORTH(0, -1, 0),
        SOUTH(0, 1, 0),
        WEST(-1, 0, 0),
        EAST(1, 0, 0),
        NONE(0, 0, 0);

        public final int dx, dy, dz;

        Direction(int dx, int dy, int dz) {
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
        }

        public Vector3i offset() {
            return new Vector3i(dx, dy, dz);
        }

        public static List<Direction> getValues(){
            return List.of(UP, DOWN, NORTH, SOUTH, WEST, EAST);
        }
    }

}