package ru.mrbedrockpy.renderer.resource;

import lombok.RequiredArgsConstructor;
import ru.mrbedrockpy.renderer.api.IResourceManager;
import ru.mrbedrockpy.renderer.api.ResourceHandle;
import ru.mrbedrockpy.renderer.graphics.Texture;
import ru.mrbedrockpy.renderer.util.graphics.TextureUtil;

import java.util.*;

@RequiredArgsConstructor
public class TextureLoader {
    private final IResourceManager rm;
    private final Map<String, Texture> cache = new HashMap<>();

    private static final String TEXTURES_ROOT = "assets/textures/";

    public Map<String, Texture> loadAll(String base) {
        String root = normalizeDir(base);
        List<ResourceHandle> list = rm.list(root, name -> name.endsWith(".png"));
        for (ResourceHandle h : list) {
            String full = normalizePath(h.path());
            String relative = stripPrefix(full, TEXTURES_ROOT);
            if (relative.equals(full)) {
                relative = stripPrefix(full, root);
            }

            if (relative.endsWith(".png")) {
                relative = relative.substring(0, relative.length() - 4);
            }

            String id = normalizeId(relative);
            cache.put(id, load(id));
        }

        return Collections.unmodifiableMap(cache);
    }

    public Texture load(String id) {
        id = normalizeId(id);
        if (cache.containsKey(id)) return cache.get(id);
        return TextureUtil.fromBufferedImage(rm.readImage(TEXTURES_ROOT + id + ".png"));
    }

    public Set<Map.Entry<String, Texture>> getAll() {
        return cache.entrySet();
    }

    private String normalizeId(String id) {
        String s = normalizePath(id);

        s = stripPrefix(s, TEXTURES_ROOT);
        s = stripPrefix(s, "textures/");
        s = stripPrefix(s, "assets/");

        if (s.endsWith(".png")) s = s.substring(0, s.length() - 4);
        return s;
    }

    private static String normalizePath(String p) {
        String s = p.replace('\\', '/');
        if (s.startsWith("/")) s = s.substring(1);
        return s;
    }

    private static String normalizeDir(String p) {
        String s = normalizePath(p);
        if (!s.endsWith("/")) s = s + "/";
        return s;
    }

    private static String stripPrefix(String s, String prefix) {
        String p = normalizePath(prefix);
        if (!p.endsWith("/")) p = p + "/";
        return s.startsWith(p) ? s.substring(p.length()) : s;
    }
}