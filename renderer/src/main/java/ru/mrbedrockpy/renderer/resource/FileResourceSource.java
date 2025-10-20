package ru.mrbedrockpy.renderer.resource;

import ru.mrbedrockpy.renderer.api.ResourceSource;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record FileResourceSource(Path root) implements ResourceSource {

    @Override
    public InputStream open(String path) {
        try {
            Path p = root.resolve(path).normalize();
            if (Files.isRegularFile(p)) return Files.newInputStream(p);
        } catch (Exception ignored) {
        }
        return null;
    }

    @Override
    public boolean exists(String path) {
        return Files.exists(root.resolve(path).normalize());
    }

    @Override
    public List<String> list(String directory) {
        Path dir = root.resolve(directory).normalize();
        if (!Files.isDirectory(dir)) return List.of();
        try (var s = Files.list(dir)) {
            return s.map(p -> p.getFileName().toString() + (Files.isDirectory(p) ? "/" : ""))
                    .sorted().toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public String id() {
        return "fs:" + root.toAbsolutePath();
    }
}