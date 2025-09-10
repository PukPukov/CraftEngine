package ru.mrbedrockpy.renderer.util.graphics;

import com.google.gson.JsonArray;
import ru.mrbedrockpy.renderer.graphics.Mesh;

import java.util.ArrayList;
import java.util.List;

public class MeshUtil {

    public static float calculateAO(boolean side1, boolean side2, boolean corner) {
        if (side1 && side2) return 0.5f;
        int occlusion = (side1 ? 1 : 0) + (side2 ? 1 : 0) + (corner ? 1 : 0);
        return switch (occlusion) { case 0 -> 1.0f; case 1 -> 0.8f; case 2 -> 0.65f; default -> 0.5f; };
    }

    public static float[][] parseVerts(JsonArray arr) {
        float[][] v = new float[4][3];
        for (int i = 0; i < Math.min(4, arr.size()); i++) {
            JsonArray p = arr.get(i).getAsJsonArray();
            v[i][0] = p.get(0).getAsFloat();
            v[i][1] = p.get(1).getAsFloat();
            v[i][2] = p.get(2).getAsFloat();
        }
        return v;
    }
}
