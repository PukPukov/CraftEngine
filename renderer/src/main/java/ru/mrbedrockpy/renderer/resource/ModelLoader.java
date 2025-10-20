package ru.mrbedrockpy.renderer.resource;

import com.google.gson.*;
import lombok.RequiredArgsConstructor;
import org.joml.Vector3f;
import ru.mrbedrockpy.craftengine.core.world.block.Block;
import ru.mrbedrockpy.renderer.api.IResourceManager;
import ru.mrbedrockpy.renderer.api.ResourceHandle;
import ru.mrbedrockpy.renderer.graphics.model.Bone;
import ru.mrbedrockpy.renderer.graphics.model.Cuboid;
import ru.mrbedrockpy.renderer.graphics.model.Model;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RequiredArgsConstructor
public class ModelLoader {

    private final IResourceManager rm;
    private final Map<String, Model> cache = new HashMap<>();

    private final ThreadLocal<Deque<String>> stackTL = ThreadLocal.withInitial(ArrayDeque::new);

    public Model load(String id) {
        id = normalizeId(id);
        if (cache.containsKey(id)) return cache.get(id);

        // читаем JSON этого id
        JsonObject jo = openJson("assets/models/" + id + ".json");
        if (jo == null) throw new IllegalStateException("Model not found: " + id);

        // resolve parent (если есть)
        String parentId = jo.has("parent") ? normalizeId(jo.get("parent").getAsString()) : null;

        // защита от циклов
        Deque<String> stack = stackTL.get();
        if (stack.contains(id)) {
            throw new IllegalStateException("Parent cycle detected: " + stack + " -> " + id);
        }

        Model result;
        stack.push(id);
        try {
            if (parentId != null && !parentId.isBlank()) {
                Model parent = load(parentId);             // рекурсивно достраиваем родителя
                result = deepCopy(parent);
                applyChildOverrides(result, jo);           // применяем изменения ребёнка
            } else {
                result = fromJson(jo);                     // чистая сборка
            }
        } finally {
            stack.pop();
        }

        cache.put(id, result);
        return result;
    }
    private InputStream tryOpen(String path) {
        return rm.open(path);
    }

    private InputStream resolveAndOpen(String id) {
        id = normalizeId(id);

        // поддержка namespace: ns:path -> assets/ns/models/path.json
        String ns = null, path = id;
        int colon = id.indexOf(':');
        if (colon >= 0) {
            ns = id.substring(0, colon);
            path = id.substring(colon + 1);
        }

        // кандидаты
        List<String> candidates = new ArrayList<>();
        // если уже полный путь вроде models/foo/bar
        candidates.add(path + ".json");
        candidates.add("models/" + path + ".json");

        if (ns != null) {
            candidates.add("assets/" + ns + "/models/" + path + ".json");
        }

        // (опционально) можно добавить ещё один кандидат с префиксом assets/minecraft/... если нужно

        for (String p : candidates) {
            InputStream in = tryOpen(p);
            if (in != null) return in;
        }
        return null;
    }

    private JsonObject openJsonById(String id) {
        try (InputStream in = resolveAndOpen(id)) {
            if (in == null) return null;
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    /** Публичный безопасный вариант, не бросает, а вернёт null */
    public synchronized Model loadOrNull(String id) {
        id = normalizeId(id);
        if (cache.containsKey(id)) return cache.get(id);

        JsonObject jo = openJsonById(id);
        if (jo == null) return null;

        String parentId = jo.has("parent") ? jo.get("parent").getAsString() : null;

        Deque<String> stack = stackTL.get();
        if (stack.contains(id)) throw new IllegalStateException("Parent cycle: " + stack + " -> " + id);

        Model result;
        stack.push(id);
        try {
            if (parentId != null && !parentId.isBlank()) {
                Model parent = load(parentId); // можно оставить строгий load здесь
                result = deepCopy(parent);
                applyChildOverrides(result, jo);
            } else {
                result = fromJson(jo);
            }
        } finally { stack.pop(); }

        cache.put(id, result);
        return result;
    }

    public Map<String, Model> loadAll(String base) {
        String dir = base.endsWith("/") ? base : base + "/";
        List<ResourceHandle> list = rm.list(dir, name -> name.endsWith(".json"));
        for (ResourceHandle h : list) {
            String full = h.path();
            String id = full.substring(full.lastIndexOf('/') + 1, full.length() - ".json".length());
            load(id);
        }
        return Collections.unmodifiableMap(cache);
    }

    private String normalizeId(String id) {
        String s = id.replace('\\', '/');
        if (s.endsWith(".json")) s = s.substring(0, s.length() - 5);
        if (s.startsWith("/")) s = s.substring(1);
        return s;
    }

    private JsonObject openJson(String path) {
        try (var in = rm.open(path)) {
            if (in == null) return null;
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            return null;
        }
    }

    private Model fromJson(JsonObject obj) {
        Model m = new Model();
        if (!obj.has("bones")) return m;

        for (JsonElement be : obj.getAsJsonArray("bones")) {
            JsonObject bj = be.getAsJsonObject();

            Bone bone = new Bone(bj.get("name").getAsString());

            if (bj.has("position")) setVec3(bone.getPosition(), bj.getAsJsonArray("position"));
            if (bj.has("rotation")) setVec3(bone.getRotation(), bj.getAsJsonArray("rotation"));

            if (bj.has("cuboids")) {
                for (JsonElement ce : bj.getAsJsonArray("cuboids")) {
                    JsonObject cj = ce.getAsJsonObject();
                    Cuboid c = new Cuboid();
                    if (cj.has("position")) setVec3(c.getPosition(), cj.getAsJsonArray("position"));
                    if (cj.has("size"))     setVec3(c.getSize(),     cj.getAsJsonArray("size"));
                    if (cj.has("faces"))    readFaces(c.getFaces(),  cj.getAsJsonObject("faces"));
                    bone.addCuboid(c);
                }
            }
            m.addBone(bone);
        }
        return m;
    }

    private void applyChildOverrides(Model target, JsonObject childJson) {
        if (!childJson.has("bones")) return;

        for (JsonElement be : childJson.getAsJsonArray("bones")) {
            JsonObject bj = be.getAsJsonObject();
            String name = bj.get("name").getAsString();

            Bone existing = target.findBone(name);
            if (existing == null) {
                // добавляем новую кость «как есть»
                Model tmp = fromJson(jsonWithSingleBone(bj));
                target.addBone(tmp.getBones().get(0));
                continue;
            }

            if (bj.has("position")) setVec3(existing.getPosition(), bj.getAsJsonArray("position"));
            if (bj.has("rotation")) setVec3(existing.getRotation(), bj.getAsJsonArray("rotation"));

            if (bj.has("cuboids")) {
                existing.getCuboids().clear();
                for (JsonElement ce : bj.getAsJsonArray("cuboids")) {
                    JsonObject cj = ce.getAsJsonObject();
                    Cuboid c = new Cuboid();
                    if (cj.has("position")) setVec3(c.getPosition(), cj.getAsJsonArray("position"));
                    if (cj.has("size"))     setVec3(c.getSize(),     cj.getAsJsonArray("size"));
                    if (cj.has("faces"))    readFaces(c.getFaces(),  cj.getAsJsonObject("faces"));
                    existing.addCuboid(c);
                }
            }
        }
    }

    private JsonObject jsonWithSingleBone(JsonObject boneObj) {
        JsonObject root = new JsonObject();
        JsonArray arr = new JsonArray();
        arr.add(boneObj);
        root.add("bones", arr);
        return root;
    }

    private void setVec3(Vector3f v, JsonArray a) {
        v.set(a.get(0).getAsFloat(), a.get(1).getAsFloat(), a.get(2).getAsFloat());
    }

    private void readFaces(Map<Block.Direction, Cuboid.UV> out, JsonObject faces) {
        for (Map.Entry<String, JsonElement> e : faces.entrySet()) {
            Block.Direction face = Block.Direction.valueOf(e.getKey().toUpperCase(Locale.ROOT));
            JsonObject uvj = e.getValue().getAsJsonObject();
            Cuboid.UV uv = new Cuboid.UV();
            uv.setU(getF(uvj, "u")); uv.setV(getF(uvj, "v"));
            uv.setW(getF(uvj, "w")); uv.setH(getF(uvj, "h"));
            out.put(face, uv);
        }
    }

    private float getF(JsonObject o, String k){ return o.has(k) ? o.get(k).getAsFloat() : 0f; }

    /** Глубокая копия (кости/кубоиды/UV/вектора) */
    private Model deepCopy(Model src) {
        Model m = new Model();
        for (Bone b : src.getBones()) {
            Bone nb = new Bone(b.getName());
            nb.getPosition().set(b.getPosition());
            nb.getRotation().set(b.getRotation());
            for (Cuboid c : b.getCuboids()) {
                Cuboid nc = new Cuboid();
                nc.getPosition().set(c.getPosition());
                nc.getSize().set(c.getSize());
                c.getFaces().forEach((face, uv) -> {
                    Cuboid.UV uv2 = new Cuboid.UV();
                    uv2.setU(uv.getU()); uv2.setV(uv.getV());
                    uv2.setW(uv.getW()); uv2.setH(uv.getH());
                    nc.getFaces().put(face, uv2);
                });
                nb.addCuboid(nc);
            }
            m.addBone(nb);
        }
        return m;
    }
}
