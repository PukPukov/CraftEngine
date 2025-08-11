package ru.mrbedrockpy.renderer.api;

import com.google.gson.JsonObject;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public interface IResourceManager extends Closeable {
    InputStream open(String path);

    String readString(String path);

    BufferedImage readImage(String path);

    boolean exists(String path);

    List<ResourceHandle> list(String directory, Predicate<String> nameFilter);

    List<ResourceSource> sources();

    void push(ResourceSource source);

    void remove(ResourceSource source);

    void clear();

    default void reload() throws IOException {}

    JsonObject getModel(String path);
}