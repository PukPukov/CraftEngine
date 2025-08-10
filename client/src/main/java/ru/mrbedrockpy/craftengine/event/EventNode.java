package ru.mrbedrockpy.craftengine.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class EventNode<E extends Event> {

    @Getter private final Class<E> event;

    private final List<Listener<E>> listeners;

    public EventNode(Class<E> event) {
        this.event = event;
        this.listeners = new ArrayList<>();
    }

    public void listen(Event event) {
        if (this.event.isInstance(event)) return;
        for (Listener<E> listener: listeners) listener.execute((E) event);
    }

    public void addListener(Listener<E> listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener<E> listener) {
        listeners.remove(listener);
    }

    public List<Listener<E>> getListeners() {
        return new ArrayList<>(listeners);
    }
}
