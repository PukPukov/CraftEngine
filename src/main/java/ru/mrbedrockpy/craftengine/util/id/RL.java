package ru.mrbedrockpy.craftengine.util.id;

import org.jetbrains.annotations.NotNull;

// ResourceLocation
public record RL(String namespace, String path) {
    public RL {
        if (namespace.isEmpty()) throw new IllegalArgumentException("Namespace cannot be empty");
        if (path.isEmpty()) throw new IllegalArgumentException("Path cannot be empty");
    }

    @NotNull
    @Override
    public String toString() {
        return namespace + ":" + path;
    }

    public String toPath(){
        return namespace + "/" + path;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof RL(String namespace1, String path1)){
            return path1.equals(path) && namespace1.equals(namespace);
        }else {
            return false;
        }
    }

    public static RL of(String namespace, String path) {
        return new RL(namespace, path);
    }

    public static RL of(String path){
        return new RL("craftengine", path);
    }
}