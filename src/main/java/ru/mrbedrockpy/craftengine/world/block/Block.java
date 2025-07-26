package ru.mrbedrockpy.craftengine.world.block;


import org.joml.Vector3i;
import ru.mrbedrockpy.craftengine.graphics.FaceData;
import ru.mrbedrockpy.craftengine.graphics.Mesh;
import ru.mrbedrockpy.craftengine.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class Block {
    private final boolean solid;

    public Block(boolean solid) {
        this.solid = solid;
    }

    public boolean isSolid() {
        return solid;
    }

    public AABB getAABB(int x, int y, int z) {
        return new AABB(x, y, z, x + 1, y + 1, z + 1);
    }


    public enum Direction {
        NORTH(0, 0, -1),
        SOUTH(0, 0, 1),
        WEST(-1, 0, 0),
        EAST(1, 0, 0),
        UP(0, 1, 0),
        DOWN(0, -1, 0),
        NONE(0, 0, 0);

        public final int dx, dy, dz;

        Direction(int dx, int dy, int dz) {
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
        }

        public Vector3i offset(int x, int y, int z) {
            return new Vector3i(x + dx, y + dy, z + dz);
        }
    }

}