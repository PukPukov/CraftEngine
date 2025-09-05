package ru.mrbedrockpy.renderer.util.graphics;

import ru.mrbedrockpy.renderer.graphics.Mesh;

import java.util.ArrayList;
import java.util.List;

public class MeshUtil {
    public static Mesh merge(List<Mesh.Data> meshDataList) {
        List<Float> combinedPositions = new ArrayList<>();
        List<Float> combinedUVs = new ArrayList<>();
        List<Float> combinedAOs = new ArrayList<>();

        for (Mesh.Data data : meshDataList) {
            float[] vertices = data.vertices();
            float[] uvs = data.uvs();
            float[] aos = data.aos();

            for (int i = 0; i < vertices.length; i += 3) {
                combinedPositions.add(vertices[i]);
                combinedPositions.add(vertices[i + 1]);
                combinedPositions.add(vertices[i + 2]);
            }

            for (int i = 0; i < uvs.length; i += 2) {
                combinedUVs.add(uvs[i]);
                combinedUVs.add(uvs[i + 1]);
            }

            for (float ao : aos) {
                combinedAOs.add(ao);
            }
        }


        float[] finalVertices = new float[combinedPositions.size()];
        float[] finalUVs = new float[combinedUVs.size()];
        float[] finalAOs = new float[combinedAOs.size()];

        for (int i = 0; i < finalVertices.length; i++) finalVertices[i] = combinedPositions.get(i);
        for (int i = 0; i < finalUVs.length; i++) finalUVs[i] = combinedUVs.get(i);
        for (int i = 0; i < finalAOs.length; i++) finalAOs[i] = combinedAOs.get(i);

        return new Mesh().vertices(finalVertices).uvs(finalUVs).aos(finalAOs);
    }

}
