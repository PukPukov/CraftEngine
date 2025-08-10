package ru.mrbedrockpy.craftengine.event;

import java.util.ArrayList;
import java.util.List;

public class EventManager {

    private final List<EventNode<? extends Event>> nodes = new ArrayList<>();

    public <E extends Event> void addListener(Class<E> clazz, Listener<E> listener) {
        EventNode<E> eventNode = getEventNode(clazz);
        if (eventNode == null) eventNode = new EventNode<>(clazz);
        eventNode.addListener(listener);
    }

    public <E extends Event> void removeListener(Class<E> clazz, Listener<E> listener) {
        EventNode<E> eventNode = getEventNode(clazz);
        if (eventNode == null) return;
        eventNode.removeListener(listener);
    }

    public <E extends Event> void callEvent(E event) {
        EventNode<E> eventNode = getEventNode((Class<E>) event.getClass());
        if (eventNode == null) return;
        eventNode.listen(event);
    }

    public <E extends Event> EventNode<E> getEventNode(Class<E> clazz) {
        for (EventNode<? extends Event> eventNode: nodes) {
            if (eventNode.getEvent().equals(clazz)) {
                return (EventNode<E>) eventNode;
            }
        }
        return null;
    }
}
