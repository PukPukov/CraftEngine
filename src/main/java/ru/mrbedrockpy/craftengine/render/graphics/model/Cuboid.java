package ru.mrbedrockpy.craftengine.render.graphics.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.joml.Vector2f;
import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.world.block.Block;

import java.util.EnumMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class Cuboid {

    private Vector3f position = new Vector3f();

    private Vector3f size = new Vector3f(1, 1, 1);

    private final Map<Block.Direction, UV> faces = new EnumMap<>(Block.Direction.class);

    public Cuboid(float x, float y, float z, float w, float h, float d) {
        this.position.set(x, y, z);
        this.size.set(w, h, d);
    }

    public Cuboid setAllUV(float u, float v, float w, float h) {
        for (Block.Direction face : Block.Direction.values()) {
            faces.put(face, new UV(u, v, w, h));
        }
        return this;
    }

    public Cuboid setFaceUV(Block.Direction face, float u, float v, float w, float h) {
        faces.put(face, new UV(u, v, w, h));
        return this;
    }

    public UV getFaceUV(Block.Direction face) {
        return faces.get(face);
    }

    @Override
    public String toString() {
        return "Cuboid{pos=" + position + ", size=" + size + ", faces=" + faces + "}";
    }

    @Data
    @NoArgsConstructor
    public static class UV {
        private float u, v;
        private float w, h;

        public UV(float u, float v, float width, float height) {
            this.u = u;
            this.v = v;
            this.w = width;
            this.h = height;
        }

        @Override
        public String toString() {
            return "UV{" + u + "," + v + "," + w + "," + h + "}";
        }
    }

    public Vector3f[] getVertices() {
        float x = position.x, y = position.y, z = position.z;
        float w = size.x, h = size.y, d = size.z;

        return new Vector3f[]{
                new Vector3f(x,     y,     z),     // 0
                new Vector3f(x + w, y,     z),     // 1
                new Vector3f(x + w, y + h, z),     // 2
                new Vector3f(x,     y + h, z),     // 3
                new Vector3f(x,     y,     z + d), // 4
                new Vector3f(x + w, y,     z + d), // 5
                new Vector3f(x + w, y + h, z + d), // 6
                new Vector3f(x,     y + h, z + d)  // 7
        };
    }

    public Map<Block.Direction, Vector2f[]> getUvs() {
        Map<Block.Direction, Vector2f[]> map = new EnumMap<>(Block.Direction.class);
        for (Map.Entry<Block.Direction, UV> entry : faces.entrySet()) {
            UV uv = entry.getValue();
            Vector2f[] arr = new Vector2f[]{
                    new Vector2f(uv.u, uv.v),
                    new Vector2f(uv.u + uv.w, uv.v),
                    new Vector2f(uv.u + uv.w, uv.v + uv.h),
                    new Vector2f(uv.u, uv.v + uv.h)
            };
            map.put(entry.getKey(), arr);
        }
        return map;
    }
}