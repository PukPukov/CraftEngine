package ru.mrbedrockpy.renderer.api;

import java.io.InputStream;
import java.util.List;

public interface ResourceSource extends AutoCloseable {
    InputStream open(String path);

    boolean exists(String path);

    List<String> list(String directory);

    String id();

    @Override default void close() {}
}