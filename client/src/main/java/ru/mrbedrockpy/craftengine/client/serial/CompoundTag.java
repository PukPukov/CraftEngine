package ru.mrbedrockpy.craftengine.client.serial;

import org.msgpack.core.MessageBufferPacker;
import org.msgpack.core.MessagePack;
import org.msgpack.core.MessageUnpacker;
import org.msgpack.value.ValueType;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class CompoundTag {

    private final Map<String, Object> map = new LinkedHashMap<>();

    // ---------- put/get helpers ----------
    public void putInt(String key, int v) {
        map.put(key, v);
    }

    public int getInt(String key) {
        Object o = map.get(key);
        if (o instanceof Number n) return n.intValue();
        if (o == null) return 0;
        throw new IllegalArgumentException("Field '" + key + "' is not an int: " + o.getClass());
    }

    public void putString(String key, String v) {
        map.put(key, v);
    }

    public String getString(String key) {
        Object o = map.get(key);
        return (o instanceof String s) ? s : null;
    }

    public void putCompound(String key, CompoundTag tag) {
        map.put(key, tag);
    }

    public CompoundTag getCompound(String key) {
        Object o = map.get(key);
        if (o == null) return new CompoundTag();
        if (o instanceof CompoundTag t) return t;
        if (o instanceof Map<?, ?> m) {
            CompoundTag t = new CompoundTag();
            t.map.putAll((Map<String, Object>) m);
            return t;
        }
        throw new IllegalArgumentException("Field '" + key + "' is not a CompoundTag: " + o.getClass());
    }

    public void putList(String key, List<?> list) {
        map.put(key, list);
    }

    public List<Object> getList(String key) {
        Object o = map.get(key);
        if (o == null) return Collections.emptyList();
        if (o instanceof List<?> l) return new ArrayList<>(l);
        throw new IllegalArgumentException("Field '" + key + "' is not a List: " + o.getClass());
    }

    public Set<String> getAllKeys() {
        return map.keySet();
    }

    public Object get(String key) {
        return map.get(key);
    }

    public void put(String key, Object value) {
        map.put(key, value);
    }

    public Map<String, Object> asMap() {
        return map;
    }

    // ---------- MessagePack (de)serialization ----------
    public byte[] toBytes() {
        try (MessageBufferPacker pk = MessagePack.newDefaultBufferPacker()) {
            writeObject(pk, map);
            return pk.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("CompoundTag.toBytes failed", e);
        }
    }

    public static CompoundTag fromBytes(byte[] bytes) {
        try (MessageUnpacker up = MessagePack.newDefaultUnpacker(bytes)) {
            Object root = readObject(up);
            if (!(root instanceof Map)) {
                throw new IllegalArgumentException("Top-level msgpack value is not a map");
            }
            CompoundTag tag = new CompoundTag();
            // уже нормализовано в write/read, но на всякий случай:
            for (var e : ((Map<String, Object>) root).entrySet()) {
                tag.map.put(e.getKey(), e.getValue());
            }
            return tag;
        } catch (IOException e) {
            throw new RuntimeException("CompoundTag.fromBytes failed", e);
        }
    }

    private static void writeObject(MessageBufferPacker pk, Object v) throws IOException {
        if (v == null) {
            pk.packNil();
        } else if (v instanceof Boolean b) {
            pk.packBoolean(b);
        } else if (v instanceof Byte || v instanceof Short || v instanceof Integer) {
            pk.packInt(((Number) v).intValue());
        } else if (v instanceof Long l) {
            pk.packLong(l);
        } else if (v instanceof Float f) {
            pk.packFloat(f);
        } else if (v instanceof Double d) {
            pk.packDouble(d);
        } else if (v instanceof String s) {
            pk.packString(s);
        } else if (v instanceof CompoundTag t) {
            // map<String, Object>
            Map<String,Object> m = t.map;
            pk.packMapHeader(m.size());
            for (var e : m.entrySet()) {
                pk.packString(e.getKey());
                writeObject(pk, e.getValue());
            }
        } else if (v instanceof Map<?,?> m) {
            // нормализуем ключ к String
            pk.packMapHeader(m.size());
            for (var e : m.entrySet()) {
                pk.packString(String.valueOf(e.getKey()));
                writeObject(pk, e.getValue());
            }
        } else if (v instanceof List<?> l) {
            pk.packArrayHeader(l.size());
            for (Object x : l) writeObject(pk, x);
        } else if (v.getClass().isArray()) {
            int len = java.lang.reflect.Array.getLength(v);
            pk.packArrayHeader(len);
            for (int i = 0; i < len; i++) writeObject(pk, java.lang.reflect.Array.get(v, i));
        } else {
            // fallback — строкой
            pk.packString(String.valueOf(v));
        }
    }

    // === Internal: decoding ===
    private static Object readObject(MessageUnpacker up) throws IOException {
        ValueType t = up.getNextFormat().getValueType();
        switch (t) {
            case NIL:
                up.unpackNil();
                return null;
            case BOOLEAN:
                return up.unpackBoolean();
            case INTEGER:
                // msgpack int/long — читаем в long, но если влазит в int — вернём int
                long L = up.unpackLong();
                if (L >= Integer.MIN_VALUE && L <= Integer.MAX_VALUE) return (int) L;
                return L;
            case FLOAT:
                // double покрывает float
                return up.unpackDouble();
            case STRING:
                return up.unpackString();
            case BINARY: {
                int len = up.unpackBinaryHeader();
                byte[] bs = new byte[len];
                up.readPayload(bs);
                return bs;
            }
            case ARRAY: {
                int n = up.unpackArrayHeader();
                List<Object> list = new ArrayList<>(n);
                for (int i = 0; i < n; i++) list.add(readObject(up));
                return list;
            }
            case MAP: {
                int n = up.unpackMapHeader();
                Map<String,Object> m = new LinkedHashMap<>(n);
                for (int i = 0; i < n; i++) {
                    // ключ как String
                    ValueType kt = up.getNextFormat().getValueType();
                    String key;
                    if (kt == ValueType.STRING) {
                        key = up.unpackString();
                    } else if (kt == ValueType.INTEGER) {
                        long k = up.unpackLong();
                        key = Long.toString(k);
                    } else {
                        // на всякий случай
                        Object ko = readObject(up);
                        key = String.valueOf(ko);
                    }
                    Object val = readObject(up);
                    // auto-wrap вложенных map в CompoundTag? — не обязательно.
                    // Храним как Map; getCompound умеет это понимать.
                    m.put(key, val);
                }
                // для удобства — заворачиваем в CompoundTag-совместимый Map
                CompoundTag tMap = new CompoundTag();
                tMap.map.putAll(m);
                return tMap.map; // возвращаем Map, а не сам CompoundTag
            }
            default:
                // не должно сюда попасть
                up.skipValue();
                return null;
        }
    }
}
