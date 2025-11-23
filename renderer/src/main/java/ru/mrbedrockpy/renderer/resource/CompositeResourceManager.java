package ru.mrbedrockpy.renderer.resource;

import lombok.Getter;
import ru.mrbedrockpy.craftengine.core.util.id.RL;
import ru.mrbedrockpy.renderer.api.IResourceManager;
import ru.mrbedrockpy.renderer.api.ResourceHandle;
import ru.mrbedrockpy.renderer.api.ResourceSource;
import ru.mrbedrockpy.renderer.util.graphics.TextureUtil;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.List;
import java.util.function.Predicate;

import javax.imageio.ImageIO;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CompositeResourceManager implements IResourceManager {
    private final List<ResourceSource> sourceList = new ArrayList<>();
    @Getter
    private final ModelLoader modelLoader = new ModelLoader(this);
    @Getter
    private final TextureLoader textureLoader = new TextureLoader(this);

    @Override
    public synchronized InputStream open(String path) {
        for (ResourceSource s : sourceList) {
            try {
                InputStream in = s.open(normalize(path));
                if (in != null) return in;
            } catch (Exception ignored) {}
        }
        return null;
    }

    @Override
    public synchronized String readString(String path) {
        try (InputStream in = open(path)) {
            if (in == null) return "";
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public synchronized BufferedImage readImage(String path) {
        try (InputStream in = open(path)) {
            if (in == null) return null;
            BufferedImage img = ImageIO.read(in);
            return img;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public synchronized boolean exists(String path) {
        for (ResourceSource s : sourceList) {
            try {
                if (s.exists(normalize(path))) return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    @Override
    public synchronized List<ResourceHandle> list(String directory, Predicate<String> nameFilter) {
        String root = normalizeDir(directory);
        LinkedHashSet<String> dedup = new LinkedHashSet<>();    // дедуп по полному пути
        ArrayList<ResourceHandle> out = new ArrayList<>();
        HashSet<String> visitedDirs = new HashSet<>();          // защитимся от повторов директорий

        for (ResourceSource s : sourceList) {
            try {
                recurseList(s, root, nameFilter, dedup, out, visitedDirs);
            } catch (Exception ignored) {}
        }
        return out;
    }

    private void recurseList(ResourceSource s,
                             String dir,
                             Predicate<String> nameFilter,
                             Set<String> dedup,
                             List<ResourceHandle> out,
                             Set<String> visitedDirs) {
        String normDir = normalizeDir(dir);

        String dirKey = System.identityHashCode(s) + ":" + normDir;
        if (!visitedDirs.add(dirKey)) return;

        List<String> names;
        try {
            names = s.list(normDir);
        } catch (Exception e) {
            return;
        }
        if (names == null || names.isEmpty()) return;

        for (String name : names) {
            if (name == null || name.isEmpty()) continue;

            String childPath = normDir + name;
            String childDir  = normalizeDir(childPath);

            boolean isDir = false;
            try {
                List<String> sub = s.list(childDir);
                isDir = (sub != null && !sub.isEmpty());
            } catch (Exception ignored) {
            }

            if (isDir) {
                recurseList(s, childDir, nameFilter, dedup, out, visitedDirs);
            } else {
                if (nameFilter != null && !nameFilter.test(name)) continue;

                if (dedup.add(childPath)) {
                    out.add(new ResourceHandle(childPath, s));
                }
            }
        }
    }

    public void load() {
        long t0 = System.nanoTime();
        int[] counters = new int[2];
        List<String> errors = new ArrayList<>();

        modelLoader.loadAll("assets/models/");
        textureLoader.loadAll(RL.of("assets/textures/"));

        long ms = (System.nanoTime() - t0) / 1_000_000L;
        if (!errors.isEmpty()) {
            for (String err : errors) System.err.println("[CompositeResourceManager] " + err);
        }
    }

    @Override
    public synchronized List<ResourceSource> sources() {
        return Collections.unmodifiableList(sourceList);
    }

    @Override
    public synchronized void push(ResourceSource source) {
        if (source == null) return;
        sourceList.addFirst(source);
    }

    @Override
    public synchronized void remove(ResourceSource source) {
        if (source == null) return;
        if (sourceList.remove(source)) {
            try { source.close(); } catch (Exception ignored) {}
        }
    }

    @Override
    public synchronized void clear() {
        for (ResourceSource s : sourceList) {
            try { s.close(); } catch (Exception ignored) {}
        }
        sourceList.clear();
    }

    @Override
    public synchronized void close() {
        clear();
    }

    private static String normalize(String path) {
        return path == null ? "" : path.replace('\\', '/');
    }
    private static String normalizeDir(String directory) {
        String d = normalize(directory);
        if (d.isEmpty()) return "";
        return d.endsWith("/") ? d : d + "/";
    }
}