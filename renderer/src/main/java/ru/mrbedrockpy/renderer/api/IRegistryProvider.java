package ru.mrbedrockpy.renderer.api;

public interface IRegistryProvider<T> {
    T get(String name);
    T getById(int id);
    String getName(T value);
    int getId(T value);
}
