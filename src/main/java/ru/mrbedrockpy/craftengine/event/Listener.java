package ru.mrbedrockpy.craftengine.event;

@FunctionalInterface
public interface Listener<T extends Event> {
    void execute(T event);
}