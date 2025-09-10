package ru.mrbedrockpy.craftengine.world.chunk;

import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class ChunkLoadManager {

    private static final class Tracked {
        final Vector2i pos;
        int lastRequestedTick;
        Tracked(Vector2i pos, int tick) {
            this.pos = pos;
            this.lastRequestedTick = tick;
        }
    }

    // Конфиг
    private final int chunkSize;
    private final int viewDistanceChunks;
    private final int unloadDelayTicks;

    private int tickCounter = 0;

    private final Map<Vector2i, Tracked> loaded = new HashMap<>(1024);

    private final List<Consumer<Vector2i>> onLoad  = new CopyOnWriteArrayList<>();
    private final List<Consumer<Vector2i>> onUnload= new CopyOnWriteArrayList<>();

    public void subscribeOnLoad(Consumer<Vector2i> l)   { onLoad.add(l); }
    public void unsubscribeOnLoad(Consumer<Vector2i> l) { onLoad.remove(l); }

    public void subscribeOnUnload(Consumer<Vector2i> l) { onUnload.add(l); }
    public void unsubscribeOnUnload(Consumer<Vector2i> l){ onUnload.remove(l); }

    public ChunkLoadManager(int chunkSize, int viewDistanceChunks, int unloadDelayTicks) {
        if (chunkSize <= 0) throw new IllegalArgumentException("chunkSize must be > 0");
        if (viewDistanceChunks < 0) throw new IllegalArgumentException("viewDistanceChunks must be >= 0");
        if (unloadDelayTicks < 0) throw new IllegalArgumentException("unloadDelayTicks must be >= 0");
        this.chunkSize = chunkSize;
        this.viewDistanceChunks = viewDistanceChunks;
        this.unloadDelayTicks = unloadDelayTicks;
    }

    public int chunkSize()          { return chunkSize; }
    public int viewDistanceChunks() { return viewDistanceChunks; }
    public int unloadDelayTicks()   { return unloadDelayTicks; }

    public void tick(Collection<Vector2i> playersPoses) {
        tickCounter++;

        PriorityQueue<ChunkWithPriority> desiredPQ =
                new PriorityQueue<>(Comparator.comparingInt(c -> c.priority));
        Set<Vector2i> desiredSet = new HashSet<>(4096);

        if (playersPoses != null && !playersPoses.isEmpty()) {
            for (Vector2i p : playersPoses) {
                fillAround(p, viewDistanceChunks, desiredPQ, desiredSet);
            }
        }

        while (!desiredPQ.isEmpty()) {
            ChunkWithPriority cwp = desiredPQ.poll();
            Vector2i p = cwp.pos;

            Tracked t = loaded.get(p);
            if (t == null) {
                Vector2i key = new Vector2i(p);
                loaded.put(key, new Tracked(new Vector2i(p), tickCounter));
                fireLoad(key);
            } else {
                t.lastRequestedTick = tickCounter;
            }
        }

        if (!loaded.isEmpty()) {
            List<Vector2i> keys = new ArrayList<>(loaded.keySet());
            for (Vector2i key : keys) {
                Tracked t = loaded.get(key);
                if (t == null) continue;

                if (!desiredSet.contains(t.pos)) {
                    if (tickCounter - t.lastRequestedTick > unloadDelayTicks) {
                        loaded.remove(key);
                        fireUnload(t.pos);
                    }
                }
            }
        }
    }

    public Set<Vector2i> getLoadedChunksSnapshot() {
        Set<Vector2i> out = new HashSet<>(loaded.size());
        for (Tracked t : loaded.values()) out.add(new Vector2i(t.pos));
        return out;
    }

    private void fireLoad(Vector2i pos)   { for (var l : onLoad)   safeCall(l, pos); }
    private void fireUnload(Vector2i pos) { for (var l : onUnload) safeCall(l, pos); }

    private static void safeCall(Consumer<Vector2i> l, Vector2i pos) {
        try { l.accept(new Vector2i(pos)); }
        catch (Throwable t) { t.printStackTrace(); }
    }

    private static void fillAround(Vector2i pos,
                                   int r,
                                   PriorityQueue<ChunkWithPriority> pq,
                                   Set<Vector2i> unique) {
        final int cx = pos.x;
        final int cz = pos.y;

        for (int dz = -r; dz <= r; dz++) {
            for (int dx = -r; dx <= r; dx++) {
                int ccx = cx + dx;
                int ccz = cz + dz;

                Vector2i v = new Vector2i(ccx, ccz);
                if (!unique.add(v)) continue;

                int prio = Math.abs(dx) + Math.abs(dz);

                pq.add(new ChunkWithPriority(v, prio));
            }
        }
    }

    private static final class ChunkWithPriority {
        final Vector2i pos; // не мутировать!
        final int priority;
        ChunkWithPriority(Vector2i pos, int priority) {
            this.pos = pos; this.priority = priority;
        }
    }
}