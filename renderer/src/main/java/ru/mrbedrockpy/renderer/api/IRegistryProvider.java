package ru.mrbedrockpy.renderer.api;

public interface IRegistryProvider<T> {
    T get(String name);
    T get(int id);
    String getName(T value);
    int getId(T value);
}