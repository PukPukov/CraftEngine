package ru.mrbedrockpy.craftengine.resource;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.Getter;
import ru.mrbedrockpy.renderer.api.IResourceManager;
import ru.mrbedrockpy.renderer.api.ResourceHandle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelLoader {
    private final IResourceManager resourceManager;
    @Getter
    private final Map<String, JsonObject> models = new HashMap<>();
    private final Gson gson = new Gson();

    public ModelLoader(IResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    public void load() {
        List<ResourceHandle> list = resourceManager.list("assets/models", name -> name.endsWith(".json"));

        for (ResourceHandle handle : list) {
            try {
                String jsonStr = handle.readString();
                JsonObject obj = gson.fromJson(jsonStr, JsonObject.class);

                String fullPath = handle.path();
                String name = fullPath
                        .replaceFirst("^assets/models/", "")
                        .replaceFirst("\\.json$", "");

                models.put(name, obj);
                System.out.println("Loaded model: " + name);
            } catch (Exception e) {
                System.err.println("Failed to load model from " + handle + ": " + e.getMessage());
            }
        }
    }

    public JsonObject getModel(String name){
        return models.get(name);
    }
}