package ru.mrbedrockpy.renderer.api;

import org.joml.Vector3i;
import ru.mrbedrockpy.renderer.phys.AABB;

public interface IBlock {
    boolean isSolid();
    AABB getAABB(int x, int y, int z);


    enum Direction {
        UP(0, 1, 0),
        DOWN(0, -1, 0),
        NORTH(0, 0, -1),
        SOUTH(0, 0, 1),
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
    }
}
