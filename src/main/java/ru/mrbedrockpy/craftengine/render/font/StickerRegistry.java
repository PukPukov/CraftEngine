package ru.mrbedrockpy.craftengine.render.font;

import ru.mrbedrockpy.craftengine.render.RenderInit;
import ru.mrbedrockpy.craftengine.render.graphics.FreeTextureAtlas;
import ru.mrbedrockpy.craftengine.util.id.RL;

import java.util.HashMap;
import java.util.Map;

public final class StickerRegistry {
    public static final StickerRegistry INSTANCE = new StickerRegistry();

    public static final class Sticker {
        public final String name;
        public final float aspect;      // w/h
        Sticker(String name, float aspect){
            this.name = name;
            this.aspect = aspect;
        }
    }

    private final Map<String, Sticker> map = new HashMap<>();

    public boolean registerByKey(String name, FreeTextureAtlas atlas, RL key) {
        float[] uvs = atlas.getNormalizedUvs(key);
        if (uvs == null || uvs.length < 8) return false;

        float u0 = uvs[0];
        float v0 = uvs[1];
        float u1 = uvs[4];
        float v1 = uvs[5];

        float aspect = (v1 - v0) / (u1 - u0);

        map.put(name, new Sticker(name, aspect));
        return true;
    }

    public Sticker get(String name) { return map.get(name); }
    public boolean contains(String name) { return map.containsKey(name); }
    public int size() { return map.size(); }

    public static void registerAllStickers(FreeTextureAtlas atlas) {
        int ok = 0, miss = 0;

        for (var entry : RenderInit.RESOURCE_MANAGER.getTextureLoader().getAll()) {
            RL key = entry.getKey(); // RL(namespace, path)
            String path = key.path(); // например: "sticker/smile" или "sticker/ui/warn"

            if (!path.startsWith("sticker/")) continue;

            // имя стикера без префикса "sticker/"
            String name = path.substring("sticker/".length());

            if (name.endsWith(".png")) name = name.substring(0, name.length() - 4);
            boolean okOne = StickerRegistry.INSTANCE.registerByKey(name, atlas, key);
            if (okOne) ok++; else miss++;
        }

        System.out.println("[StickerRegistry] registered=" + ok + " missing=" + miss);
    }
}
