package ru.mrbedrockpy.craftengine.client.event;

@FunctionalInterface
public interface Listener<T extends Event> {
    void execute(T event);
}