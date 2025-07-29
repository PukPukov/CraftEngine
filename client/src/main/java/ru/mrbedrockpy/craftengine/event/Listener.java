package ru.mrbedrockpy.craftengine.event;

public interface Listener <T extends Event> {
    void execute(T event);

}
