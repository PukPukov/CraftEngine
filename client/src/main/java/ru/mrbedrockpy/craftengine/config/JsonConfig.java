package ru.mrbedrockpy.craftengine.config;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

// getFloat() потому что нельзя сделать метод float() и тд.
public class JsonConfig {
    private static final Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();
    
    private final Path file;
    private JsonObject root;
    
    private JsonConfig(Path file) {
        this.file = file;
        this.root = new JsonObject();
        if (Files.exists(file)) {
            loadFromFile();
        }
    }
    
    public static JsonConfig load(String filePath) {
        return load(Path.of(filePath));
    }
    
    public static JsonConfig load(String filePath, Map<String, Object> defaults) {
        return load(Path.of(filePath), defaults);
    }
    
    public static JsonConfig load(Path file) {
        JsonConfig cfg = new JsonConfig(file);
        
        if (Files.notExists(file)) {
            try {
                Path parent = file.getParent();
                if (parent != null && Files.notExists(parent)) {
                    Files.createDirectories(parent);
                }
                cfg.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return cfg;
    }
    
    public static JsonConfig load(Path file, Map<String, Object> defaults) {
        JsonConfig cfg = new JsonConfig(file);
        
        try {
            Path parent = file.getParent();
            if (parent != null && Files.notExists(parent)) {
                Files.createDirectories(parent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        defaults.forEach((key, value) -> {
            if (!cfg.root.has(key)) {
                JsonElement el = GSON.toJsonTree(value);
                cfg.root.add(key, el);
            }
        });
        
        cfg.save();
        return cfg;
    }
    
    private void loadFromFile() {
        try (Reader reader = Files.newBufferedReader(file);
             JsonReader jsonReader = new JsonReader(reader)) {
            JsonElement el = JsonParser.parseReader(jsonReader);
            if (el.isJsonObject()) {
                root = el.getAsJsonObject();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void save() {
        try (Writer writer = Files.newBufferedWriter(file)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public int getInt(String key, int def) {
        JsonElement el = root.get(key);
        if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
            try {
                return el.getAsInt();
            } catch (NumberFormatException ignore) {}
        }
        return def;
    }
    
    public float getFloat(String key, float def) {
        JsonElement el = root.get(key);
        if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
            try {
                return el.getAsFloat();
            } catch (NumberFormatException ignore) {}
        }
        return def;
    }
    
    public boolean getBoolean(String key, boolean def) {
        JsonElement el = root.get(key);
        if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isBoolean()) {
            return el.getAsBoolean();
        }
        return def;
    }
    
    public String getString(String key, String def) {
        JsonElement el = root.get(key);
        if (el != null && el.isJsonPrimitive()) {
            return el.getAsString();
        }
        return def;
    }
    
    public void set(String key, int value) {
        root.addProperty(key, value);
    }
    
    public void set(String key, float value) {
        root.addProperty(key, value);
    }
    
    public void set(String key, boolean value) {
        root.addProperty(key, value);
    }
    
    public void set(String key, String value) {
        root.addProperty(key, value);
    }
    
    public void set(String key, Object value) {
        JsonElement el = GSON.toJsonTree(value);
        root.add(key, el);
    }
    
    public <T> T getObject(String key, Class<T> clazz, T def) {
        JsonElement el = root.get(key);
        if (el != null && !el.isJsonNull()) {
            try {
                return GSON.fromJson(el, clazz);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }
        return def;
    }
}