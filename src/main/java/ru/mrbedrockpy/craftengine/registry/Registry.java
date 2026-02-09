package ru.mrbedrockpy.craftengine.registry;


import ru.mrbedrockpy.craftengine.util.id.RL;

import java.util.*;

public class Registry<T> {
    private final Map<RL, T> nameToObject = new HashMap<>();
    private final Map<T, RL> objectToName = new HashMap<>();
    private final Map<T, Integer> objectToId = new HashMap<>();
    private final List<T> idToObject = new ArrayList<>();
    private boolean frozen = false;

    public <U extends T>U register(RL name, U value) {
        if (frozen) throw new IllegalStateException("Registry is frozen");
        if (nameToObject.containsKey(name)) throw new IllegalArgumentException("Already registered: " + name);

        int id = idToObject.size();
        nameToObject.put(name, value);
        objectToName.put(value, name);
        idToObject.add(value);
        objectToId.put(value, id);
        return value;
    }

    public T get(RL name) {
        return nameToObject.get(name);
    }

    public int getId(T value) {
        return objectToId.getOrDefault(value, -1);
    }

    public T get(int id) {
        if (id < 0 || id >= idToObject.size()) throw new IllegalStateException("No such entry");
        T entry = idToObject.get(id);
        return entry;
    }

    public RL getRL(T value) {
        return objectToName.get(value);
    }

    public void freeze() {
        if (frozen) throw new IllegalStateException("Already frozen");
        frozen = true;
    }

    public int size() {
        return idToObject.size();
    }
}