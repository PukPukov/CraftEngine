package ru.mrbedrockpy.craftengine.server.world;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class TickSystem {

    @Getter
    private final int tps;
    @Getter
    private final float tickTime;
    private double accumulator = 0f;
    @Getter
    private long currentTick = 0;

    private final List<TickListener> listeners = new ArrayList<>();

    public interface TickListener {
        void onTick();
    }

    public TickSystem(int tps) {
        this.tps = tps;
        this.tickTime = 1.0f / tps;
    }

    public void addListener(TickListener listener) {
        listeners.add(listener);
    }

    public void update(double deltaTime) {
        accumulator += deltaTime;

        while (accumulator >= tickTime) {
            currentTick++;
            for (TickListener listener : listeners) {
                listener.onTick();
            }
            accumulator -= tickTime;
        }
    }

    public double partialTick() {
        return accumulator / ((double) tickTime);
    }
}