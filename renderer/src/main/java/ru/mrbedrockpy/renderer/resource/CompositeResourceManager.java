package ru.mrbedrockpy.renderer.resource;

import lombok.Getter;
import ru.mrbedrockpy.craftengine.core.util.Logger;
import ru.mrbedrockpy.renderer.api.IResourceManager;
import ru.mrbedrockpy.renderer.api.ResourceHandle;
import ru.mrbedrockpy.renderer.api.ResourceSource;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;

import javax.imageio.ImageIO;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CompositeResourceManager implements IResourceManager {
    private final List<ResourceSource> sourceList = new ArrayList<>();
    @Getter
    private final ModelLoader modelLoader = new ModelLoader(this);
    private final Logger logger = Logger.getLogger(CompositeResourceManager.class);

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
        String dir = normalizeDir(directory);
        Set<String> dedup = new LinkedHashSet<>();
        List<ResourceHandle> out = new ArrayList<>();

        for (ResourceSource s : sourceList) {
            try {
                List<String> names = s.list(dir);
                if (names == null || names.isEmpty()) continue;

                for (String name : names) {
                    if (name == null || name.isEmpty()) continue;
                    if (nameFilter != null && !nameFilter.test(name)) continue;

                    if (dedup.add(name)) {
                        out.add(new ResourceHandle(dir + name, s));
                    }
                }
            } catch (Exception ignored) {}
        }
        return out;
    }

    public void load() {
        long t0 = System.nanoTime();
        int[] counters = new int[2];
        List<String> errors = new ArrayList<>();

        try {
           modelLoader.loadAllUnder("assets/models/");
        } catch (Exception e) {
            errors.add("ModelLoader.load() failed: " + e.getMessage());
        }

        long ms = (System.nanoTime() - t0) / 1_000_000L;
        logger.info("[CompositeResourceManager] Loaded: sources=" + sourceList.size()
                + ", files=" + counters[0]
                + ", models=" + counters[1]
                + ", time=" + ms + " ms"
                + (errors.isEmpty() ? "" : ", errors=" + errors.size()));
        if (!errors.isEmpty()) {
            for (String err : errors) logger.error("[CompositeResourceManager] " + err);
        }
    }

    @Override
    public synchronized List<ResourceSource> sources() {
        return Collections.unmodifiableList(sourceList);
    }

    @Override
    public synchronized void push(ResourceSource source) {
        if (source == null) return;
        sourceList.add(0, source);
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