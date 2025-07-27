package ru.mrbedrockpy.craftengine.window;

public class FPSCounter {
    private long lastTime;
    private int frames;
    private int fps;

    public FPSCounter() {
        this.lastTime = System.currentTimeMillis();
        this.frames = 0;
        this.fps = 0;
    }

    public void update() {
        frames++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime >= 1000) {
            fps = frames;
            frames = 0;
            lastTime = currentTime;
        }
    }

    public int getFPS() {
        return fps;
    }
}
