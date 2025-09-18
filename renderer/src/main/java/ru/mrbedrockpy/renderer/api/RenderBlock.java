package ru.mrbedrockpy.renderer.api;

import lombok.Getter;
import org.joml.Vector3i;

public class RenderBlock {
    @Getter
    private final String id;
    @Getter
    private final boolean solid;
    public RenderBlock(String id, boolean solid){
        this.id = id;
        this.solid = solid;
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
    }
}