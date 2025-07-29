package ru.mrbedrockpy.craftengine.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventManager {
    private final Map<Class<? extends Event>, CopyOnWriteArrayList<Listener<? extends Event>>> listeners = new HashMap<>();

    public <T extends Event> void addListener(Class<T> clazz, Listener<T> listener) {
        listeners.computeIfAbsent(clazz, k -> new CopyOnWriteArrayList<>()).add(listener);
    }

    public <T extends Event> void removeListener(Class<T> clazz, Listener<T> listener) {
        List<Listener<? extends Event>> list = listeners.get(clazz);
        if (list != null) {
            list.remove(listener);
            if (list.isEmpty()) {
                listeners.remove(clazz);
            }
        }
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