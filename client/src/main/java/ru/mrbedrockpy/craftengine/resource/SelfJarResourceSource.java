package ru.mrbedrockpy.craftengine.resource;

import ru.mrbedrockpy.renderer.api.ResourceSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SelfJarResourceSource implements ResourceSource {
    private final Path jarPath;
    private final Map<String, List<String>> dirIndex = new HashMap<>();

    public SelfJarResourceSource() {
        try {
            URI uri = SelfJarResourceSource.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI();
            this.jarPath = Paths.get(uri);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось определить путь до текущего JAR", e);
        }
        index();
    }

    private void index() {
        try (ZipFile zip = new ZipFile(jarPath.toFile())) {
            Enumeration<? extends ZipEntry> e = zip.entries();
            while (e.hasMoreElements()) {
                ZipEntry ze = e.nextElement();
                String name = ze.getName().replace('\\', '/');
                if (ze.isDirectory()) continue;

                // Добавляем файл в индекс директории
                int slash = name.lastIndexOf('/');
                String dir = (slash >= 0) ? name.substring(0, slash + 1) : "";
                String fileName = (slash >= 0) ? name.substring(slash + 1) : name;

                dirIndex.computeIfAbsent(dir, k -> new ArrayList<>()).add(fileName);
            }
            // Добавляем дочерние директории
            dirIndex.keySet().forEach(dir -> {
                int slash = dir.lastIndexOf('/', dir.length() - 2);
                while (slash >= 0) {
                    String parent = dir.substring(0, slash + 1);
                    String folder = dir.substring(slash + 1);
                    if (!folder.isEmpty() && !folder.endsWith("/")) folder += "/";
                    dirIndex.computeIfAbsent(parent, k -> new ArrayList<>()).add(folder);
                    slash = parent.lastIndexOf('/', parent.length() - 2);
                }
            });
            dirIndex.values().forEach(list -> list.sort(String::compareTo));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось проиндексировать текущий JAR: " + jarPath, e);
        }
    }

    @Override
    public InputStream open(String path) {
        String norm = path.replace('\\', '/');
        try (ZipFile zip = new ZipFile(jarPath.toFile())) {
            ZipEntry entry = zip.getEntry(norm);
            if (entry != null && !entry.isDirectory()) {
                return Files.newInputStream(jarPath.resolve(norm));
            }
        } catch (IOException ignored) {}
        return null;
    }

    @Override
    public boolean exists(String path) {
        String norm = path.replace('\\', '/');
        try (ZipFile zip = new ZipFile(jarPath.toFile())) {
            return zip.getEntry(norm) != null;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public List<String> list(String directory) {
        String dir = directory.replace('\\', '/');
        if (!dir.isEmpty() && !dir.endsWith("/")) dir += "/";
        return dirIndex.getOrDefault(dir, List.of());
    }

    @Override
    public String id() {
        return "self-jar:" + jarPath.toAbsolutePath();
    }
}