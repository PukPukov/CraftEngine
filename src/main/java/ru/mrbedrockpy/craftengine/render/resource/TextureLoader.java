package ru.mrbedrockpy.craftengine.render.resource;

import lombok.RequiredArgsConstructor;
import ru.mrbedrockpy.craftengine.render.api.IResourceManager;
import ru.mrbedrockpy.craftengine.render.api.ResourceHandle;
import ru.mrbedrockpy.craftengine.render.graphics.Texture;
import ru.mrbedrockpy.craftengine.render.util.graphics.TextureUtil;
import ru.mrbedrockpy.craftengine.util.id.RL;

import java.util.*;

@RequiredArgsConstructor
public class TextureLoader {
    private final IResourceManager rm;
    private final Map<RL, Texture> cache = new HashMap<>();

    private static final String ASSETS_PREFIX   = "assets/";
    private static final String TEXTURES_DIR    = "textures/";

    public Map<RL, Texture> loadAll(RL base) {
        final String root = toTexturesDir(base); // "assets/ns/textures/<basePath>/"

        // 1) берём всё, без преждевременного фильтра
        List<ResourceHandle> raw = rm.list(root, name -> true);

        System.out.println("[TextureLoader] root=" + root + " handles=" + raw.size());
        for (ResourceHandle h : raw) {
            String p = normalizePath(h.path());
            String pLower = p.toLowerCase(Locale.ROOT);

            // 2) пропускаем только png (проверяем по ПУТИ, а не по "name")
            if (!pLower.endsWith(".png")) continue;

            // 3) строим RL устойчиво к абсолютным и относительным путям
            RL id = rlFromAnyPath(p, base);

            // 4) грузим
            cache.put(id, load(id));
        }
        return Collections.unmodifiableMap(cache);
    }

    private static RL rlFromAnyPath(String path, RL base) {
        String s = normalizePath(path);

        // Абсолютный?
        if (s.startsWith("assets/")) {
            s = s.substring("assets/".length());
            int slash = s.indexOf('/');
            if (slash < 0) throw new IllegalArgumentException("Invalid absolute path: " + path);
            String ns = s.substring(0, slash);
            s = s.substring(slash + 1);
            if (s.startsWith("textures/")) s = s.substring("textures/".length());
            if (s.endsWith(".png")) s = s.substring(0, s.length() - 4);
            return RL.of(ns, s);
        }

        // Относительный: считаем относительно "base"
        String basePath = normalizePath(base.path());
        String ns = base.namespace();

        // склеиваем: <base.path>/<relative> и чистим "textures/" + ".png" на всякий
        String merged = (basePath.isEmpty() ? s : (basePath + "/" + s));
        if (merged.startsWith("textures/")) merged = merged.substring("textures/".length());
        if (merged.endsWith(".png")) merged = merged.substring(0, merged.length() - 4);

        return RL.of(ns, merged);
    }

    public Texture load(RL id) {
        Texture cached = cache.get(id);
        if (cached != null) return cached;

        String filePath = ASSETS_PREFIX + id.namespace() + "/" + TEXTURES_DIR + id.path() + ".png";
        var v = rm.readImage(filePath);
        if(v == null) throw new NullPointerException("texture: " + id + " is null");
        Texture tex = TextureUtil.fromBufferedImage(v);
        cache.put(id, tex);
        return tex;
    }

    public Set<Map.Entry<RL, Texture>> getAll() {
        return cache.entrySet();
    }

    private static String toTexturesDir(RL baseDir) {
        String ns = baseDir.namespace();
        String p  = normalizePath(baseDir.path());

        // убираем возможные лишние префиксы в любом порядке
        p = stripPrefixIfStarts(p, "assets/");
        p = stripPrefixIfStarts(p, ns + "/");
        p = stripPrefixIfStarts(p, "textures/");

        // уберём конечный '/'
        if (p.endsWith("/")) p = p.substring(0, p.length() - 1);

        // итоговый корень без обязательного слэша в конце (так совместимее с разными RM)
        return ASSETS_PREFIX + ns + "/" + TEXTURES_DIR + (p.isEmpty() ? "" : "/" + p);
    }

    private static String stripPrefixIfStarts(String s, String prefix) {
        String p = normalizePath(prefix);
        return s.startsWith(p) ? s.substring(p.length()) : s;
    }

    private static String normalizePath(String p) {
        String s = p.replace('\\', '/');
        if (s.startsWith("/")) s = s.substring(1);
        return s;
    }

    private static String stripPrefixDir(String s, String prefixDir) {
        String p = normalizePath(prefixDir);
        if (!p.endsWith("/")) p = p + "/";
        return s.startsWith(p) ? s.substring(p.length()) : s;
    }
}
