package ru.mrbedrockpy.renderer.api;

public interface IRegistryProvider<T> {
    T get(String name);
    T get(int id);
    String name(T value);
    int id(T value);
}