package ru.mrbedrockpy.craftengine.client.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventManager {
    private final Map<Class<? extends Event>, List<Listener<? extends Event>>> listeners = new HashMap<>();

    public <T extends Event> void addListener(Class<T> clazz, Listener<T> listener) {
        listeners.computeIfAbsent(clazz, k -> new ArrayList<>()).add(listener);
    }

    public <T extends Event> void callEvent(T event) {
        List<Listener<? extends Event>> list = listeners.get(event.getClass());
        if (list != null) {
            for (Listener listener : list) {
                ((Listener<T>) listener).execute(event);
                if (event.isCancelled()) break;
            }
        }
    }
}