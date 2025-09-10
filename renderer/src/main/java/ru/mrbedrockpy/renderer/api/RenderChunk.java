package ru.mrbedrockpy.renderer.api;

import org.joml.Vector2i;
import ru.mrbedrockpy.renderer.graphics.Mesh;
import ru.mrbedrockpy.renderer.phys.AABB;

import java.util.Objects;

public record RenderChunk(Vector2i pos, short[][][] blocks) {
    public AABB getAABB() {
        return new AABB(pos.x * 16, pos.y * 16, 0, pos.x * 16 + 16, pos.y * 16 + 16, 16);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RenderChunk that)) return false;
        return Objects.equals(this.pos, that.pos);
    }

    public Vector2i getWorldPos() {
        return new Vector2i(pos).mul(16);
    }
}
