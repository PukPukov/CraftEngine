package ru.mrbedrockpy.craftengine.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Registry<T>{
    private final Map<String, T> registry = new HashMap<>();

    private boolean frozen = false;

    public Registry(){

    }

    public void register(String name, T value) {
        if (registry.containsKey(name)) {
            throw new IllegalArgumentException("Name already registered: " + name);
        }
        registry.put(name, value);
    }

    public T get(String name) {
        if (!registry.containsKey(name)) {
            throw new IllegalArgumentException("Name not found: " + name);
        }
        return registry.get(name);
    }

    public void freeze() {
        if (frozen) {
            throw new IllegalStateException("Registry is already frozen");
        }
        frozen = true;
    }
}
