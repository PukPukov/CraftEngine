package ru.mrbedrockpy.craftengine.render.resource;

import ru.mrbedrockpy.craftengine.render.api.ResourceSource;

import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class UrlResourceSource implements ResourceSource {
    private final ClassLoader cl;
    private final String base;

    public UrlResourceSource(ClassLoader cl, String base) {
        this.cl = (cl != null ? cl : Thread.currentThread().getContextClassLoader());
        this.base = normalizeBase(base);
    }

    private static String normalizeBase(String base) {
        String b = (base == null ? "" : base.replace('\\','/'));
        b = b.replaceFirst("^/+", "");
        return b.isEmpty() ? "" : (b.endsWith("/") ? b : b + "/");
    }

    private String full(String path) {
        String p = path.replace('\\','/').replaceFirst("^/+", "");
        return base + p;
    }

    @Override
    public InputStream open(String path) {
        return cl.getResourceAsStream(full(path));
    }

    @Override
    public boolean exists(String path) {
        return cl.getResource(full(path)) != null;
    }

    @Override
    public List<String> list(String directory) {
        try {
            String dir = full(directory);
            URL url = cl.getResource(dir);
            if (url == null) return List.of();


            if ("file".equals(url.getProtocol())) {
                // dev-режим: ресурсы лежат как обычные файлы
                Path root = Paths.get(url.toURI());
                if (!Files.isDirectory(root)) return List.of();
                try (var s = Files.list(root)) {
                    return s.map(p -> p.getFileName().toString() + (Files.isDirectory(p) ? "/" : ""))
                            .sorted()
                            .toList();
                }
            } else if ("jar".equals(url.getProtocol())) {
                // jar:file:/...!/assets/...
                JarURLConnection conn = (JarURLConnection) url.openConnection();
                try (JarFile jar = conn.getJarFile()) {
                    String prefix = conn.getEntryName();
                    if (!prefix.endsWith("/")) prefix += "/";
                    Set<String> direct = new LinkedHashSet<>();
                    String finalPrefix = prefix;
                    String finalPrefix1 = prefix;
                    jar.stream()
                            .map(JarEntry::getName)
                            .filter(n -> n.startsWith(finalPrefix) && !n.equals(finalPrefix))
                            .forEach(n -> {
                                String rest = n.substring(finalPrefix1.length());
                                int slash = rest.indexOf('/');
                                if (slash < 0) {
                                    direct.add(rest);
                                } else {
                                    direct.add(rest.substring(0, slash + 1));
                                }
                            });
                    return direct.stream().sorted().toList();
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return List.of();
    }

    @Override
    public String id() {
        return "url:" + base;
    }
}
